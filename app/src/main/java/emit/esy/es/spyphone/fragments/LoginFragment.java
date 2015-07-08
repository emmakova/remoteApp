package emit.esy.es.spyphone.fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.interfaces.UpdateUI;
import emit.esy.es.spyphone.util.FirebaseUtil;
import emit.esy.es.spyphone.util.NetworkUtil;

/**
 * Created by Emil Makovac on 23/06/2015.
 */
public class LoginFragment extends Fragment {

    Button register;
    EditText et_username, et_password;
    TextView label;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.login_fragment, container, false);

        et_username = (EditText) rootView.findViewById(R.id.et_uname);
        et_password = (EditText) rootView.findViewById(R.id.et_pass);
        label = (TextView) rootView.findViewById(R.id.label);
        label.setTextColor(Color.RED);
        register = (Button) rootView.findViewById(R.id.btn_register);
        register.setOnClickListener(btnClickListener);
        return rootView;
    }
    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!NetworkUtil.getConnectivityStatus(getActivity())){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.no_net_title);
                builder.setMessage(R.string.no_net_msg);
                builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(i);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                FirebaseUtil fu = new FirebaseUtil(getActivity(), label, new UpdateUI() {
                    @Override
                    public void updateUI(TextView t) {
                        t.setText("* Wrong username or password");

                    }
                });
                String username = String.valueOf(et_username.getText());
                String password = String.valueOf(et_password.getText());
                Log.d(username, password);
                if (username.length() <= 0 || password.length() <= 0) {
                    label.setText("*All fields are required");
                } else {
                    if(v.getId() == R.id.btn_register) {
                        Toast.makeText(getActivity(), "Authenticating...", Toast.LENGTH_SHORT).show();
                        fu.authenticateUser(username, password, false);
                    }
                }
            }
        }
    };
}
