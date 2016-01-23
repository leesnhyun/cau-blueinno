package sh.cau.blueinno_lcd.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import sh.cau.blueinno_lcd.R;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnFindDevice;
    private EditText message;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Create Objects */
        this.toolbar = (Toolbar)findViewById(R.id.toolbar);
        this.btnFindDevice = (Button)findViewById(R.id.btnFindDevice);
        this.message = (EditText)findViewById(R.id.message);
        this.btnSend = (Button)findViewById(R.id.btnSend);

        _initToolbar();
        _initActions();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /// private
    private void _initToolbar(){
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }
    }

    private void _initActions(){
        // 'find device' button action.
        this.btnFindDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( MainActivity.this, FindDeviceActivity.class );
                startActivity(i);
            }
        });

        this.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( FindDeviceActivity.findDeviceActivity != null){
                    Toast.makeText(getApplicationContext(), "전송:"+message.getText().toString(), Toast.LENGTH_LONG).show();
                    FindDeviceActivity.findDeviceActivity.OnDataChangeListener(message.getText().toString().getBytes());
                }
            }
        });

    }

}
