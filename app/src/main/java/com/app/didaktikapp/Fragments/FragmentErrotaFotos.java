package com.app.didaktikapp.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.didaktikapp.Activities.MapActivity;
import com.app.didaktikapp.BBDD.Modelos.ActividadErrota;
import com.app.didaktikapp.BBDD.database.DatabaseRepository;
import com.app.didaktikapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentErrotaFotos extends Fragment {

    private View view;

    final int REQUEST_IMAGE_CAPTURE1 = 1890;
    final int REQUEST_IMAGE_CAPTURE2 = 1891;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Long idActividad;

    private Button btnContinuar;

    private ImageView ivPregunta1,ivPregunta2;
    private Button ib1,ib2;

    private String mParam1;
    private String mParam2;

    private boolean img1,img2 = false;

    public FragmentErrotaFotos() {}


    public static FragmentErrotaFotos newInstance(Long param1) {
        FragmentErrotaFotos fragment = new FragmentErrotaFotos();
        Bundle args = new Bundle();
        args.putLong(ARG_PARAM1, param1);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // OBTENEMOS LA VIEW
        view = inflater.inflate(R.layout.fragment_errota_fotos, container, false);

        // PREGUNTAMOS POR PERMISOS PARA HACER LAS FOTOS LUEGO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
            } else {

            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 225);
            } else {

            }
        }

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE1);
        }

        //CONFIGURAMOS EL BOTON CONTINUAR
        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setText(getResources().getString(R.string.Continuar));
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guardarImagen(ivPregunta1);
                guardarImagen(ivPregunta2);

                //actualizar BD
                guardarBBDD();
                //cerrar fragment
                //actualizar marker
                getFragmentManager().beginTransaction().remove(FragmentErrotaFotos.this).commit();
            }
        });
        btnContinuar.setEnabled(false);

        crearPreguntas(view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void crearPreguntas(View view) {
        ivPregunta1 = view.findViewById(R.id.ivErrotaPregunta1);
        ivPregunta2 = view.findViewById(R.id.ivErrotaPregunta2);

        ib1 = view.findViewById(R.id.btnErrotaPregunta1);
        ib1.setOnClickListener(new ListenerBoton());

        ib2 = view.findViewById(R.id.btnErrotaPregunta2);
        ib2.setOnClickListener(new ListenerBoton());
    }

    private class ListenerBoton implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            dispatchTakePictureIntent(v);
        }

        private void dispatchTakePictureIntent(View v){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            int request;
            if(v == view.findViewById(R.id.btnErrotaPregunta1)){
                request = REQUEST_IMAGE_CAPTURE1;
            }else{
                request = REQUEST_IMAGE_CAPTURE2;
            }

            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, request);
            }
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if ((requestCode == REQUEST_IMAGE_CAPTURE1 || requestCode == REQUEST_IMAGE_CAPTURE2) && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            if (requestCode == REQUEST_IMAGE_CAPTURE1) {
                ivPregunta1.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap,1000,1000,false));
//                ib1.setVisibility(View.INVISIBLE);
                img1 = true;
            } else {
                ivPregunta2.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap,1000,1000,false));
//                ib2.setVisibility(View.INVISIBLE);
                img2 = true;
            }

            if (img1 && img2) {
                btnContinuar.setText(getResources().getString(R.string.Terminar));
                btnContinuar.setEnabled(true);
            }

        }

//        if(ivPregunta1.getDrawable() != null && ivPregunta2.getDrawable() != null){
//            view.findViewById(R.id.btnContinuar).setEnabled(true);
//        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void guardarImagen(ImageView iv){
        //to get the image from the ImageView (say iv)
        BitmapDrawable draw = (BitmapDrawable) iv.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File dir = new File(sdCard.getAbsolutePath() + "/Didaktikapp");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        Log.i("FILE",fileName);
        Log.i("DIR",dir.getPath());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "Titulo" , "descripcion");
    }

    private void guardarBBDD(){

        ActividadErrota actividadErrota = DatabaseRepository.getAppDatabase().getErrotaDao().getErrota(idActividad);

        actividadErrota.setEstado(2);

        actividadErrota.setFragment(3);

        actividadErrota.setFoto1(imageToBase64(ivPregunta1));

        actividadErrota.setFoto2(imageToBase64(ivPregunta2));

        DatabaseRepository.getAppDatabase().getErrotaDao().updateErrota(actividadErrota);

    }

    private String imageToBase64(ImageView iv){
        iv.buildDrawingCache();
        Bitmap bitmap = iv.getDrawingCache();

        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] image=stream.toByteArray();

        return  Base64.encodeToString(image, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((MapActivity)getActivity()).cambiarLocalizacion();
    }

}