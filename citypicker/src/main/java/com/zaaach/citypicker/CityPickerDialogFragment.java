package com.zaaach.citypicker;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zaaach.citypicker.adapter.CityListAdapter;
import com.zaaach.citypicker.adapter.InnerListener;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.adapter.decoration.DividerItemDecoration;
import com.zaaach.citypicker.adapter.decoration.SectionItemDecoration;
import com.zaaach.citypicker.db.DBManager;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.view.SideIndexBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Bro0cL
 * @Date: 2018/2/6 20:50
 */
public class CityPickerDialogFragment extends DialogFragment implements TextWatcher,
        View.OnClickListener, SideIndexBar.OnIndexTouchedChangedListener, InnerListener {
    private View mContentView;
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private TextView mOverlayTextView;
    private SideIndexBar mIndexBar;
    private EditText mSearchBox;
    private TextView mCancelBtn;
    private ImageView mClearAllBtn;

    private LinearLayoutManager mLayoutManager;
    private CityListAdapter mAdapter;
    private List<City> mAllCities;
    private List<HotCity> mHotCities;
    private List<HotCity> mHistories;
    private List<City> mResults;

    private DBManager dbManager;

    private boolean enableAnim = false;
    private int mAnimStyle = R.style.DefaultCityPickerAnimation;
    private OnPickListener mOnPickListener;
    /**
     * 获取实例
     * @param enable 是否启用动画效果
     * @return
     */
    public static CityPickerDialogFragment newInstance(boolean enable){
        final CityPickerDialogFragment fragment = new CityPickerDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("cp_enable_anim", enable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_TITLE, R.style.CityPickerStyle);

        Bundle args = getArguments();
        if (args != null) {
            enableAnim = args.getBoolean("cp_enable_anim");
        }
        dbManager = new DBManager(getContext());
        mHotCities=dbManager.getHotCities();
        mHistories=dbManager.getHistoryCities();
        mAllCities = dbManager.getAllCities();
        mAllCities.add(0, new HotCity("历史车站", true, ""));
        mAllCities.add(1, new HotCity("热门车站", false, ""));
        mResults = mAllCities;
    }
    public void setAnimationStyle(@StyleRes int style){
        this.mAnimStyle =  style;
        if (mAnimStyle==0)
            mAnimStyle=R.style.DefaultCityPickerAnimation ;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.cp_dialog_city_picker, container, false);
        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.cp_city_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(getActivity(), mAllCities), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()), 1);
        mAdapter = new CityListAdapter(getActivity(), mAllCities, mHotCities, mHistories);
        mAdapter.setInnerListener(this);
        mAdapter.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mEmptyView = mContentView.findViewById(R.id.cp_empty_view);
        mOverlayTextView = (TextView) mContentView.findViewById(R.id.cp_overlay);

        mIndexBar = (SideIndexBar) mContentView.findViewById(R.id.cp_side_index_bar);
        mIndexBar.setOverlayTextView(mOverlayTextView)
                .setOnIndexChangedListener(this);

        mSearchBox = (EditText) mContentView.findViewById(R.id.cp_search_box);
        mSearchBox.addTextChangedListener(this);

        mCancelBtn = (TextView) mContentView.findViewById(R.id.cp_cancel);
        mClearAllBtn = (ImageView) mContentView.findViewById(R.id.cp_clear_all);
        mCancelBtn.setOnClickListener(this);
        mClearAllBtn.setOnClickListener(this);

        return mContentView;
    }


    /** 搜索框监听 */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        String keyword = s.toString();
        SectionItemDecoration sid= (SectionItemDecoration) mRecyclerView.getItemDecorationAt(0);
        if (TextUtils.isEmpty(keyword)){
            mClearAllBtn.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            mResults = mAllCities;
            if (sid!=null)  sid.setData(mResults);


            mAdapter.updateData(mResults);
        }else {
            mClearAllBtn.setVisibility(View.VISIBLE);
            //开始数据库查找
            mResults = dbManager.searchCity(keyword);
            if (sid!=null)  sid.setData(mResults);

            if (mResults == null || mResults.isEmpty()){
                mEmptyView.setVisibility(View.VISIBLE);
            }else {
                mEmptyView.setVisibility(View.GONE);
                mAdapter.updateData(mResults);
            }
        }
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cp_cancel) {
            dismiss(-1, null);
        }else if(id == R.id.cp_clear_all){
            mSearchBox.setText("");
        }
    }

    @Override
    public void onIndexChanged(String index, int position) {
        //判断如果正在滚动，则停止滚动
        if (mRecyclerView.getScrollState()==2){
            mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
        }
        //滚动RecyclerView到索引位置
        mAdapter.scrollToSection(index);
    }


    @Override
    public void dismiss(int position, City data) {
        dismiss();
        if (mOnPickListener != null&&data!=null){
            dbManager.updateHistoryCity(data.getCode());
            mOnPickListener.onPick(position, data);
        }
    }
    public void setOnPickListener(OnPickListener listener){
        this.mOnPickListener = listener;
    }
}
