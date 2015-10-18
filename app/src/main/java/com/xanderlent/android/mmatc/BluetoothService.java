package com.xanderlent.android.mmatc;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothService extends Service {
    private class Peer {
        private Edge edge;
        private String name;
        private BluetoothDevice device;
        private BluetoothSocket socket;
        private ClientThread thread;

        public Peer(BluetoothDevice device) {
            this.device = device;
        }

        public Peer(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void startThread() {
            thread = new ClientThread(this);
            thread.start();
        }

        public void shutdown() {
            if(thread != null) {
                thread.cancel();
            }
        }

        public Edge getEdge() {
            return edge;
        }

        public void setEdge(Edge edge) {
            this.edge = edge;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public void setDevice(BluetoothDevice device) {
            this.device = device;
        }

        public BluetoothSocket getSocket() {
            return socket;
        }

        public void setSocket(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void sendOutgoingPlane() {
            new SendThread(this).start();
        }
    }

    public class Binder extends android.os.Binder {
        public void start(String name) {
            BluetoothService.this.start(name);
        }

        public Bus getBus() {
            return bus;
        }
    }

    private static final String SERVICE_NAME = "Mobile Multi-player Air Traffic Controller (mMATC)";
    private static final UUID SERVICE_UUID = UUID.fromString("0BD9DBE8-50D3-4733-88B2-BDA3276FAF96");
    private static final int NUM_EDGES = 4;

    private Bus bus;
    private String myName;
    private boolean wantShutdown;
    private BluetoothAdapter adapter;
    private BluetoothServerSocket serverSocket;
    private Collection<Peer> peers;
    private ListenThread listenThread;

    public BluetoothService() {
        bus = new Bus();
        bus.register(this);
        wantShutdown = false;
        adapter = BluetoothAdapter.getDefaultAdapter();
        peers = Collections.synchronizedCollection(new ArrayList<Peer>(NUM_EDGES));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
    }

    public void start(String name) {
        myName = name;
        synchronizeServerStatus();
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        adapter.startDiscovery();
    }

    public void shutdown() {
        wantShutdown = true;
        synchronizeServerStatus();
        for(Peer peer : peers) {
            peer.shutdown();
        }
    }

    public Map<Edge, String> getNeighborNames() {
        Map<Edge, String> map = new HashMap<>();
        for(Peer peer : peers) {
            if(peer.getName() != null) {
                map.put(peer.getEdge(), peer.getName());
            }
        }
        return map;
    }

    private Edge findFreeEdge() {
        List<Edge> freeEdges = new ArrayList<>();
        freeEdges.add(Edge.NORTH);
        freeEdges.add(Edge.EAST);
        freeEdges.add(Edge.SOUTH);
        freeEdges.add(Edge.WEST);
        for(Peer peer : peers) {
            if(peer.getEdge() != null) {
                freeEdges.remove(peer.getEdge());
            }
        }
        if(freeEdges.isEmpty()) {
            return null;
        }
        return freeEdges.get((int)(Math.random() * freeEdges.size()));
    }

    private void synchronizeServerStatus() {
        BluetoothServerSocket serverSocket = this.serverSocket;
        boolean serverIsRunning = serverSocket != null;
        boolean serverShouldBeRunning = !wantShutdown && findFreeEdge() != null;
        if(serverIsRunning && !serverShouldBeRunning) {
            listenThread.cancel();
            listenThread = null;
        }else if(serverShouldBeRunning && !serverIsRunning) {
            try {
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
            } catch (IOException e) {
                throw new RuntimeException("can't create Bluetooth server socket", e);
            }
            this.serverSocket = serverSocket;
            listenThread = new ListenThread(serverSocket);
            listenThread.start();
        }
    }

    private void handleClient(BluetoothSocket client) {
        Peer peer = new Peer(client);
        peer.startThread();
        peers.add(peer);
        synchronizeServerStatus();
    }

    public static class IncomingPlaneEvent {
        private Edge edge;

        public IncomingPlaneEvent(Edge edge) {
            this.edge = edge;
        }

        public Edge getEdge() {
            return edge;
        }
    }

    private void notifyIncomingPlane(Peer peer) {
        bus.post(new IncomingPlaneEvent(peer.getEdge()));
    }

    public static class OutgoingPlaneEvent {
        private Edge edge;

        public OutgoingPlaneEvent(Edge edge) {
            this.edge = edge;
        }

        public Edge getEdge() {
            return edge;
        }
    }

    @Subscribe
    public void onOutgoingPlane(OutgoingPlaneEvent event) {
        Edge edge = event.getEdge();
        for(Peer peer : peers) {
            if(peer.getEdge() != null && peer.getEdge().equals(edge) && peer.getName() != null) {
                peer.sendOutgoingPlane();
            }
        }
    }

    public static class PeersChangedEvent {
        private Map<Edge, String> neighborNames;

        protected PeersChangedEvent(Map<Edge, String> neighborNames) {
            this.neighborNames = neighborNames;
        }

        public Map<Edge, String> getNeighborNames() {
            return neighborNames;
        }
    }

    private void notifyPeersChanged() {
        bus.post(producePeersChangedEvent());
    }

    @Produce
    public PeersChangedEvent producePeersChangedEvent() {
        return new PeersChangedEvent(getNeighborNames());
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Peer peer = new Peer(device);
                peer.startThread();
                peers.add(peer);
            }
        }
    };

    private class ListenThread extends Thread {
        private volatile BluetoothServerSocket serverSocket;
        private volatile boolean cancelled;

        public ListenThread(BluetoothServerSocket serverSocket) {
            this.serverSocket = serverSocket;
            cancelled = false;
        }

        @Override
        public void run() {
            while(true) {
                BluetoothServerSocket serverSocket = this.serverSocket;
                if(serverSocket == null || cancelled) {
                    break;
                }
                BluetoothSocket client;
                try {
                    client = serverSocket.accept();
                } catch (IOException e) {
                    continue;
                }
                handleClient(client);
            }
        }

        public void cancel() {
            BluetoothServerSocket serverSocket = this.serverSocket;
            cancelled = true;
            if(serverSocket != null) {
                this.serverSocket = null;
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException("cannot close Bluetooth server socket", e);
                }
            }
        }
    }

    private class ClientThread extends Thread {
        private Peer peer;
        private volatile boolean cancelled;

        public ClientThread(Peer peer) {
            this.peer = peer;
            cancelled = false;
        }

        @Override
        public void run() {
            try {
                if(peer.getSocket() == null) {
                    peer.setSocket(peer.getDevice().createInsecureRfcommSocketToServiceRecord(SERVICE_UUID));
                }
                BluetoothSocket socket = peer.getSocket();
                DataInputStream inputStream;
                DataOutputStream outputStream;
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                Edge edge = findFreeEdge();
                if(edge != null) {
                    peer.setEdge(edge);
                }
                outputStream.writeUTF(myName);
                peer.setName(inputStream.readUTF());
                notifyPeersChanged();
                while (true) {
                    int message = inputStream.read();
                    if(message == 0) {
                        notifyIncomingPlane(peer);
                    }else{
                        break;
                    }
                }
            } catch (IOException e) {
                // give up on this connection, but don't crash
            } finally {
                // FIXME: do we need to do anything else?
                peers.remove(peer);
                notifyPeersChanged();
            }
        }

        public void cancel() {
            BluetoothSocket socket = peer.getSocket();
            cancelled = true;
            if(socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private class SendThread extends Thread {
        private Peer peer;

        public SendThread(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            try {
                BluetoothSocket socket = peer.getSocket();
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.write(0);
            }catch(IOException e) {
                // ignore
            }
        }
    }
}
