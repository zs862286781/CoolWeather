package com.example.coolweather;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utulity;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    //当前所在等级
    private int currentLeave = LEVEL_PROVINCE;
    //当前所在省份
    private Province currentProvince;
    //当前所在市区
    private City currentCity;

    /*
    * 省市县列表
    * */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;


    TextView areaTitle;
    ListView areaList;
    Button backButton;

    /*
    * 列表数据源
    * */
    List<String> dataList = new ArrayList<>();
    ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        areaTitle = view.findViewById(R.id.area_title);
        backButton = view.findViewById(R.id.back_button);
        areaList = view.findViewById(R.id.area_list);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        areaList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces();
        areaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLeave == LEVEL_PROVINCE) {
                    currentProvince = provinceList.get(i);
                    queryCities();
                }else if (currentLeave == LEVEL_CITY) {
                    currentCity = cityList.get(i);
                    queryCounties();
                }else if (currentLeave == LEVEL_COUNTY) {
                    County county = countyList.get(i);
                    if (getActivity() instanceof MainActivity) {
                        WeatherActivity.starActivity(getContext(),county.getWeatherId());
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(county.getWeatherId());
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLeave == LEVEL_COUNTY) {
                    queryCities();
                }else if (currentLeave == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
    }

    /*
     * 查询全国省份，优先从本地数据库中查找
     * */
    private void queryProvinces() {
        areaTitle.setText("全国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        Log.d("provinceList","" + provinceList.size());
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province: provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            areaList.setSelection(0);
            currentLeave = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,LEVEL_PROVINCE);
        }
    }

    /*
    * 根据省份查询城市
    * */
    private void queryCities() {
        areaTitle.setText(currentProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceId = ?",String.valueOf(currentProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            areaList.setSelection(0);
            currentLeave = LEVEL_CITY;
        }else {
            String address = "http://guolin.tech/api/china/" + currentProvince.getProvinceCode();
            queryFromServer(address,LEVEL_CITY);
        }
    }

    /*
    * 根据城市查询县
    * */
    private void queryCounties() {
        areaTitle.setText(currentCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityId = ?",String.valueOf(currentCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            areaList.setSelection(0);
            currentLeave = LEVEL_COUNTY;
        }else {
            String address = "http://guolin.tech/api/china/" + currentProvince.getProvinceCode() +
                    "/" + currentCity.getCityCode();
            queryFromServer(address,LEVEL_COUNTY);
        }
    }

    private void queryFromServer(String address,int type) {
        HttpUtil.getInstance().sendRequest(address, new HttpUtil.HttpListener() {
            @Override
            public void onSuccess(String response) {
                boolean result = false;
                if (type == LEVEL_PROVINCE) {
                    result = Utulity.handleProvinceResponse(response);
                }else if (type == LEVEL_CITY) {
                    result = Utulity.handleCityResponce(response,currentProvince.getId());
                }else if (type == LEVEL_COUNTY) {
                    result = Utulity.handleCountyResponce(response,currentCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (type == LEVEL_PROVINCE) {
                                queryProvinces();
                            }else if (type == LEVEL_CITY) {
                                queryCities();
                            }else if (type == LEVEL_COUNTY) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(int code) {
            }
        });
    }
}
