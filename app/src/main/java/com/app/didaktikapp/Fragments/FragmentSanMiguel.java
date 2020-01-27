package com.app.didaktikapp.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.app.didaktikapp.Activities.MapActivity;
import com.app.didaktikapp.BBDD.Modelos.ActividadSanMiguel;
import com.app.didaktikapp.BBDD.database.DatabaseRepository;
import com.app.didaktikapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSanMiguel.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSanMiguel#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSanMiguel extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "FragmentSanMiguel";

    // TODO: Rename and change types of parameters
    private Long idActividad;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RadioGroup radioGroupPregunta1;
    private RadioButton radioPregunta1A,radioPregunta1B,radioPregunta1C;

    private RadioGroup radioGroupPregunta2;
    private RadioButton radioPregunta2A,radioPregunta2B,radioPregunta2C;

    private RadioGroup radioGroupPregunta3;
    private RadioButton radioPregunta3A,radioPregunta3B,radioPregunta3C;

    private RadioGroup radioGroupPregunta4;
    private RadioButton radioPregunta4A,radioPregunta4B,radioPregunta4C;

    private Button btnContinuar;

    private View view;
    private int colorradio;

    private int contpregunta;

    private int correctas = 0;

    public FragmentSanMiguel() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentSanMiguel.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSanMiguel newInstance(Long idActividad) {
        FragmentSanMiguel fragment = new FragmentSanMiguel();
        Bundle args = new Bundle();
        args.putLong(ARG_PARAM1, idActividad);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idActividad = getArguments().getLong(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }





    }

    private void guardarBBDD(){

        ActividadSanMiguel actividadSanMiguel = DatabaseRepository.getAppDatabase().getSanMiguelDao().getSanMiguel(idActividad);

        actividadSanMiguel.setEstado(1);

        actividadSanMiguel.setFragment(1);

        actividadSanMiguel.setTest(correctas+"/4");

        DatabaseRepository.getAppDatabase().getSanMiguelDao().updateSanMiguel(actividadSanMiguel);

    }

    private void vibrar(){
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 400 milliseconds
        v.vibrate(400);
    }

    private void primeraPregunta(){



        radioGroupPregunta1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                radioPregunta1A.setTextColor(colorradio);
                radioPregunta1B.setTextColor(colorradio);
                radioPregunta1C.setTextColor(colorradio);

                if(checkedId == R.id.rbtnPregunta1_A){
                    radioPregunta1A.setTextColor(Color.RED);
                    vibrar();
                }

                if(checkedId == R.id.rbtnPregunta1_B){
                    radioPregunta1B.setTextColor(Color.RED);
                    vibrar();
                }

                if(checkedId == R.id.rbtnPregunta1_C){
                    radioPregunta1C.setTextColor(Color.GREEN);
                    correctas++;
                    btnContinuar.setEnabled(true);
                }




            }
        });
    }

    private void segundaPregunta(){
        view.findViewById(R.id.layoutPregunta1).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.layoutPregunta2).setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(false);


        radioGroupPregunta2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                radioPregunta2A.setTextColor(colorradio);
                radioPregunta2B.setTextColor(colorradio);
                radioPregunta2C.setTextColor(colorradio);

                if(checkedId == R.id.rbtnPregunta2_A){
                    radioPregunta2A.setTextColor(Color.RED);
                    vibrar();
                }

                if(checkedId == R.id.rbtnPregunta2_B){

                    radioPregunta2B.setTextColor(Color.GREEN);
                    correctas++;
                    btnContinuar.setEnabled(true);
                }

                if(checkedId == R.id.rbtnPregunta2_C){
                    radioPregunta2C.setTextColor(Color.RED);
                    vibrar();
                }




            }
        });
    }


    private void terceraPregunta(){
        view.findViewById(R.id.layoutPregunta2).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.layoutPregunta3).setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(false);

        radioGroupPregunta3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                radioPregunta3A.setTextColor(colorradio);
                radioPregunta3B.setTextColor(colorradio);
                radioPregunta3C.setTextColor(colorradio);

                if(checkedId == R.id.rbtnPregunta3_A){
                    radioPregunta3A.setTextColor(Color.RED);
                    vibrar();
                }

                if(checkedId == R.id.rbtnPregunta3_B){

                    radioPregunta3B.setTextColor(Color.RED);
                    vibrar();
                }

                if(checkedId == R.id.rbtnPregunta3_C){

                    radioPregunta3C.setTextColor(Color.GREEN);
                    correctas++;

                    btnContinuar.setEnabled(true);
                }




            }
        });
    }

    private void cuartaPregunta(){
        view.findViewById(R.id.layoutPregunta3).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.layoutPregunta4).setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(false);


        radioGroupPregunta4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                radioPregunta4A.setTextColor(colorradio);
                radioPregunta4B.setTextColor(colorradio);
                radioPregunta4C.setTextColor(colorradio);

                if(checkedId == R.id.rbtnPregunta4_A){
                    radioPregunta4A.setTextColor(Color.RED);
                    vibrar();

                }

                if(checkedId == R.id.rbtnPregunta4_B){

                    radioPregunta4B.setTextColor(Color.GREEN);

                    btnContinuar.setEnabled(true);

                    correctas++;

                    btnContinuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Guardar en BBDD
                            guardarBBDD();

                            //Lanzar siguiente fragment
                            FragmentSanMiguelTinderKotlin fragment = FragmentSanMiguelTinderKotlin.newInstance(idActividad);
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
                            transaction.replace(R.id.fragment_frame, fragment);
                            transaction.commit();
                            transaction.addToBackStack("Fragment");
                        }
                    });
                }

                if(checkedId == R.id.rbtnPregunta4_C){
                    radioPregunta4C.setTextColor(Color.RED);
                    vibrar();
                }




            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_san_miguel, container, false);



        radioGroupPregunta1 = (RadioGroup) view.findViewById(R.id.rgroupPregunta1);
        radioPregunta1A = view.findViewById(R.id.rbtnPregunta1_A);
        colorradio = radioPregunta1A.getCurrentTextColor();
        radioPregunta1B = view.findViewById(R.id.rbtnPregunta1_B);
        radioPregunta1C = view.findViewById(R.id.rbtnPregunta1_C);


        radioGroupPregunta2 = (RadioGroup) view.findViewById(R.id.rgroupPregunta2);
        radioPregunta2A = view.findViewById(R.id.rbtnPregunta2_A);
        radioPregunta2B = view.findViewById(R.id.rbtnPregunta2_B);
        radioPregunta2C = view.findViewById(R.id.rbtnPregunta2_C);

        radioGroupPregunta3 = (RadioGroup) view.findViewById(R.id.rgroupPregunta3);
        radioPregunta3A = view.findViewById(R.id.rbtnPregunta3_A);
        radioPregunta3B = view.findViewById(R.id.rbtnPregunta3_B);
        radioPregunta3C = view.findViewById(R.id.rbtnPregunta3_C);

        radioGroupPregunta4 = (RadioGroup) view.findViewById(R.id.rgroupPregunta4);
        radioPregunta4A = view.findViewById(R.id.rbtnPregunta4_A);
        radioPregunta4B = view.findViewById(R.id.rbtnPregunta4_B);
        radioPregunta4C = view.findViewById(R.id.rbtnPregunta4_C);

        contpregunta = 1;
        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(contpregunta==1){
                    contpregunta++;
                    segundaPregunta();
                    return;
                }
                if(contpregunta==2){
                    contpregunta++;
                    terceraPregunta();
                    return;
                }
                if(contpregunta==3){
                    contpregunta++;
                    cuartaPregunta();
                    return;
                }


            }
        });

        primeraPregunta();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((MapActivity)getActivity()).cambiarLocalizacion();
    }
}
