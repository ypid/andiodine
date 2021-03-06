package org.xapek.andiodine;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class FragmentStatus extends Fragment {
    public static final String TAG = "FRAGMENT_STATUS";

    private TextView mStatus;
    private TextView mLogmessages;
    private ScrollView mScrollview;
    private Button mCancel;

    private final BroadcastReceiver broadcastReceiverStatusUpdates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got intent: " + intent);
            if (IodineVpnService.ACTION_STATUS_ERROR.equals(intent.getAction())) {
                new AlertDialog.Builder(FragmentStatus.this.getActivity())//
                        .setIcon(R.drawable.error) //
                        .setTitle(intent.getStringExtra(IodineVpnService.EXTRA_ERROR_MESSAGE)) //
                        .setMessage(mLogmessages.getText()) //
                        .create() //
                        .show();
            } else if (IodineVpnService.ACTION_STATUS_CONNECT.equals(intent.getAction())) {
                mStatus.setText("Connect");
            } else if (IodineVpnService.ACTION_STATUS_CONNECTED.equals(intent.getAction())) {
                mStatus.setText("Connected");
            } else if (IodineVpnService.ACTION_STATUS_DISCONNECT.equals(intent.getAction())) {
                mStatus.setText("Disconnect");
            }
        }
    };

    private final BroadcastReceiver broadcastReceiverLogMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IodineClient.ACTION_LOG_MESSAGE.equals(intent.getAction())) {
                mLogmessages.append("\n");
                mLogmessages.append(intent.getStringExtra(IodineClient.EXTRA_MESSAGE));
                mScrollview.fullScroll(View.FOCUS_DOWN);
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStatus = (TextView) getActivity().findViewById(R.id.status_message);
        mLogmessages = (TextView) getActivity().findViewById(R.id.status_logmessages);
        mScrollview = (ScrollView) getActivity().findViewById(R.id.status_scrollview);
        mCancel = (Button) getActivity().findViewById(R.id.status_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDisconnect();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilterStatusUpdates = new IntentFilter();
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_CONNECT);
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_CONNECTED);
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_DISCONNECT);
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_ERROR);
        getActivity().registerReceiver(broadcastReceiverStatusUpdates, intentFilterStatusUpdates);

        Intent intent = new Intent(IodineVpnService.ACTION_CONTROL_UPDATE);
        getActivity().sendBroadcast(intent);

        IntentFilter intentFilterLogMessages = new IntentFilter();
        intentFilterLogMessages.addAction(IodineClient.ACTION_LOG_MESSAGE);
        getActivity().registerReceiver(broadcastReceiverLogMessages, intentFilterLogMessages);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiverStatusUpdates);
        getActivity().unregisterReceiver(broadcastReceiverLogMessages);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, null);
    }

    private void requestDisconnect() {
        Intent intent = new Intent(IodineVpnService.ACTION_CONTROL_DISCONNECT);
        getActivity().sendBroadcast(intent);
    }
}
