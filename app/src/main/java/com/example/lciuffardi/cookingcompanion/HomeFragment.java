package com.example.lciuffardi.cookingcompanion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String name = null;
    private String url = null;
    private int randomRecipe = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_layout, container, false);

        if (mListener != null){
            mListener.onFragmentInteraction("Home");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Random r = new Random();

        try {
            if(!preferences.getString("Today", "").isEmpty()){
                Date savedDate = ((Date) df.parse(preferences.getString("Today", "")));
                today = df.parse(df.format(today));
                if(savedDate.before(today)){
                    editor.putString("Today", df.format(today));
                    editor.putInt("RandomSelection", r.nextInt(6-1) + 1);
                    editor.putInt("RandomRecipe", r.nextInt(999-1)+1);
                    editor.commit();
                }
            }else{
                editor.putString("Today", df.format(today));
                editor.putInt("RandomSelection", r.nextInt(6-1) + 1);
                editor.putInt("RandomRecipe", r.nextInt(999-1)+1);
                editor.commit();
            }

        }catch(ParseException ex){
            ex.printStackTrace();
        }
        preferences.getInt("RandomRecipe", 0);
        randomRecipe(preferences.getInt("RandomSelection", 0), view);



        ListView listView = (ListView) view.findViewById(
                R.id.expiring_ingredients_listView);
        IngredientsDatabaseManager dbMgr = new IngredientsDatabaseManager(getActivity()
                .getApplicationContext());

        final Cursor cursor = dbMgr.getExpiredIngredientCursor(dbMgr.getReadableDatabase());
        getActivity().startManagingCursor(cursor);

        ListAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.two_line_list_item,
                cursor,
                new String[] {IngredientsDatabaseManager.NAME,
                        IngredientsDatabaseManager.EXPIRATION},
                new int[] {android.R.id.text1, android.R.id.text2},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View itemView = super.getView(position, convertView, parent);
                try {
                    cursor.moveToPosition(position);
                    Date today = Calendar.getInstance().getTime();
                    Calendar warning = Calendar.getInstance();
                    warning.setTime(today);
                    warning.add(Calendar.DAY_OF_YEAR, 3);

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String date = cursor.getString(cursor.getColumnIndex(IngredientsDatabaseManager.EXPIRATION));
                    String[] restructureDate = date.split("/");
                    date = restructureDate[2] + "-" + restructureDate[0] + "-" + restructureDate[1] + " " + "00:00:00";
                    Date expiration = df.parse(date);


                    if (today.after(expiration))
                        itemView.setBackgroundColor(Color.RED);
                    else if(warning.getTime().after(expiration))
                        itemView.setBackgroundColor(Color.YELLOW);
                    else
                        itemView.setBackgroundColor(Color.GREEN);
                }
                catch(ParseException ex){
                    ex.printStackTrace();
                }
                return itemView;
            }
        };


        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                                            View view, int position, long id) {
                        Intent intent = new Intent(
                                getActivity().getApplicationContext(),
                                IngredientDetail.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    }
                });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String title) {
        if (mListener != null) {
            mListener.onFragmentInteraction(title);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String title);
    }

    private void randomRecipe(int num, View view) {

        TextView recipeName = (TextView) view.findViewById(R.id.dish_name_textView);
        ImageView recipeImage = (ImageView) view.findViewById(R.id.dish_imageView);
        JSONObject recipesJSON = null;
        JSONArray listArr = null;
        JSONObject listObj = null;
        Map<String, String> recipeMap = new HashMap<>();
        String selection = null;
        try {
            recipesJSON = new JSONObject(JSONLoader.loadRecipesJSON(getActivity().getApplicationContext()));


        switch (num) {
            case 1:
                selection = "Desserts";
                break;
            case 2:
                selection = "Meat";
                break;
            case 3:
                selection = "Pasta";
                break;
            case 4:
                selection = "Salad";
                break;
            case 5:
                selection = "Soup";
                break;
            case 6:
                selection = "Vegetables";
                break;
        }
            listArr = recipesJSON.optJSONArray(selection);
            listObj = listArr.getJSONObject(randomRecipe % listArr.length());
            name = listObj.getString("name");
            url = listObj.getString("url");
            int resID = getResources().getIdentifier(listObj.getString("img"), "mipmap", getActivity().getPackageName());
            recipeImage.setImageResource(resID);


        } catch(JSONException e){
            e.printStackTrace();
        }

        recipeName.setText(name);
        recipeName.setOnClickListener(this);
        recipeImage.setOnClickListener(this);
    }

    public void onClick(final View v) { //check for what button is pressed
        Intent intent = new Intent(
                getActivity().getApplicationContext(),
                RecipeDetails.class);
        switch (v.getId()) {
            case R.id.dish_imageView:

                intent.putExtra("url", url);
                intent.putExtra("name", name);
                startActivity(intent);
                break;
            case R.id.dish_name_textView:
                intent.putExtra("url", url);
                intent.putExtra("name", name);
                startActivity(intent);
                break;
        }
    }
}
