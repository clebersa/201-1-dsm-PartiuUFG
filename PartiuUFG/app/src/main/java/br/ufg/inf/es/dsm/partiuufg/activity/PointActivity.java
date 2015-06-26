package br.ufg.inf.es.dsm.partiuufg.activity;

import android.database.sqlite.SQLiteException;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.ufg.inf.es.dsm.partiuufg.R;
import br.ufg.inf.es.dsm.partiuufg.dbModel.SingleBusStop;
import br.ufg.inf.es.dsm.partiuufg.fragment.NextPointBusTimeFragment;
import br.ufg.inf.es.dsm.partiuufg.http.EasyBusService;
import br.ufg.inf.es.dsm.partiuufg.http.RestBusServiceFactory;
import br.ufg.inf.es.dsm.partiuufg.model.Point;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PointActivity extends AbstractActivity {
    private Integer pointNumber;
    private Point point;
    private NextPointBusTimeFragment fragment;

    public void updatePointViewInformation() {
        TextView address = (TextView) findViewById(R.id.tvAddress);
        address.setText(point.getAddress());
        TextView searchTime = (TextView) findViewById(R.id.tvSearchTime);
        searchTime.setText(getString(R.string.last_search_time) + " " + point.getSearchDateFormatted());
    }

    public void increasePointAccess() {
        List<SingleBusStop> accessList;
        try {
            accessList = SingleBusStop.find(SingleBusStop.class,
                    "number = ?", pointNumber.toString());
        } catch(SQLiteException e) {
            accessList = new ArrayList<SingleBusStop>();
        }

        if(accessList.size() > 0 ) {
            for (SingleBusStop access : accessList) {
                access.setAddress(point.getAddress());
                access.setReference(point.getReferenceLocation());
                access.setLastSearchDate(point.getSearchDate());
                access.setAccessCount(access.getAccessCount() + 1);
                access.save();
            }
        } else {
            SingleBusStop access = new SingleBusStop(pointNumber, point.getAddress(),
                    point.getReferenceLocation(), point.getSearchDate(), (long) 1);
            access.save();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pointNumber = getIntent().getIntExtra("pointId", -1);

        if(savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            fragment = new NextPointBusTimeFragment();
            ft.add(R.id.linhas, fragment);
            ft.commit();

            EasyBusService service = RestBusServiceFactory.getAdapter();
            service.getPoint(pointNumber.toString(), new Callback<Point>() {
                @Override
                public void success(Point vPoint, Response response) {
                    point = vPoint;
                    increasePointAccess();
                    updatePointViewInformation();
                    fragment.setPoint(point);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast toast = Toast.makeText(getBaseContext(),
                            "Ponto não encontrado", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        } else {
            try {
                point = (Point) savedInstanceState.getSerializable("point");
                updatePointViewInformation();
            } catch( NullPointerException e) {}
        }
    }

    @Override
    protected void setActivityContentView() {
        setContentView(R.layout.activity_point);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        super.onQueryTextSubmit(query);
        finish();
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("point", point);
        super.onSaveInstanceState(outState);
    }
}
