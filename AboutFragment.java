package com.omninos.safekidz.Fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.omninos.safekidz.Activities.DeviceInfoActivity;
import com.omninos.safekidz.Activities.SettingActivity;
import com.omninos.safekidz.Common_Classes.AppConstant;
import com.omninos.safekidz.Common_Classes.ChildGetAndUpdateProfileModel;
import com.omninos.safekidz.R;
import com.omninos.safekidz.Retrofit.Api;
import com.omninos.safekidz.Retrofit.ApiClient;
import com.omninos.safekidz.Utills.CommonUtil;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    private DeviceInfoActivity activity;
    private TextInputEditText dateOfBirth, ChildName, ChildMail, ChildNumber;
    private String S_Date, S_Name, S_Mail, S_Number, S_Gender, Device_id, p, path, RelationShip;
    private ImageView Cal, ChildImage;
    private Calendar myCalendar;
    private Spinner Gender;
    private Button Save;
    private ScrollView HideKey;

    public AboutFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        Device_id = AppConstant.getModel().getDevice_id();
        ChildName = v.findViewById(R.id.childName);
        ChildMail = v.findViewById(R.id.ChildemailEdit);
        ChildNumber = v.findViewById(R.id.childNumber);
        ChildImage = v.findViewById(R.id.ChildImage);
        Gender = v.findViewById(R.id.gender);

        Save = v.findViewById(R.id.Save);

        HideKey = v.findViewById(R.id.scroll);

        HideKey.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });

        activity = (DeviceInfoActivity) getActivity();
        myCalendar = Calendar.getInstance();

        dateOfBirth = v.findViewById(R.id.childDateOfbirth);
        Cal = v.findViewById(R.id.calenderData);
        dateOfBirth.setEnabled(false);

        ChildImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Choose();
            }
        });
        Gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                S_Gender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        GetProfile();

        Cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(
                        activity, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker,
                                          int selectedyear, int selectedmonth,
                                          int selectedday) {

                        mcurrentDate.set(Calendar.YEAR, selectedyear);
                        mcurrentDate.set(Calendar.MONTH, selectedmonth);
                        mcurrentDate.set(Calendar.DAY_OF_MONTH,
                                selectedday);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

                        dateOfBirth.setText(sdf.format(mcurrentDate
                                .getTime()));
                    }
                }, mYear, mMonth, mDay);

                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                mDatePicker.setTitle("Date of Birth");
                mDatePicker.show();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveChildData();
            }
        });
        return v;
    }

    private void Choose() {

        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.custom_dialog_for_img, null);


        Button camera = (Button) v.findViewById(R.id.choose_camera);
        Button gallery = (Button) v.findViewById(R.id.choose_gallery);
        Button cancel = (Button) v.findViewById(R.id.choose_cancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(v);
        final AlertDialog alertDialog = builder.create();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraData();
                alertDialog.cancel();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mygallery();
                alertDialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();


    }

    private void cameraData() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 101);
        }
    }


    private void Mygallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 100);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 100:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                        ChildImage.setImageBitmap(bitmap);

                        Log.d("onActivityResult: ", getRealPathFromUri(selectedImage));
                        p = getRealPathFromUri(selectedImage);
                        CommonUtil.SaveImgPath(activity, p);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;

                case 101:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ChildImage.setImageBitmap(imageBitmap);
                    // Log.d( "onActivityResult: FromData", String.valueOf(data.getData()));
                    Log.d("onActivityResult: FromMethod", String.valueOf(getImageUri(activity, imageBitmap)));
                    Uri captureImage = getImageUri(activity, imageBitmap);
                    p = getRealPathFromUri(captureImage);
                    CommonUtil.SaveImgPath(activity, p);
                    Log.d("onActivityResult: Path", p);

            }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromUri(Uri tempUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getActivity().getContentResolver().query(tempUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void SaveChildData() {
        S_Date = dateOfBirth.getText().toString();
        S_Name = ChildName.getText().toString();
        S_Mail = ChildMail.getText().toString();
        S_Number = ChildNumber.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,4}";
        if (!S_Mail.matches(emailPattern)) {
            ChildMail.setError("enter valid email");
        } else if (S_Number.length() > 12) {
            ChildNumber.setError("enter valid length of number");
        } else {

            MultipartBody.Part body;

            RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), S_Name);
            RequestBody mailBody = RequestBody.create(MediaType.parse("text/plain"), S_Mail);
            RequestBody numberBody = RequestBody.create(MediaType.parse("text/plain"), S_Number);
            RequestBody dateBody = RequestBody.create(MediaType.parse("text/plain"), S_Date);
            RequestBody genderBody = RequestBody.create(MediaType.parse("text/plain"), S_Gender);
            RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), Device_id);
            RequestBody relationBody = RequestBody.create(MediaType.parse("text/plain"), RelationShip);

            if (p == null) {
                path = CommonUtil.GetImgPath(activity);
                File file = new File(path);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("image", "", requestFile);
            } else {
                File file = new File(p);
                Log.e("Imagepath&&&&&&&&&&&&&&", p);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            }


            if (CommonUtil.isNetworkConnected(activity)) {
                CommonUtil.showProgress(activity, "Please wait...");
                Api api = ApiClient.apiclient().create(Api.class);
                Call<ChildGetAndUpdateProfileModel> call = api.UpdateChildProfile(idBody, nameBody, dateBody, genderBody, numberBody, relationBody, mailBody, body);

                call.enqueue(new Callback<ChildGetAndUpdateProfileModel>() {
                    @Override
                    public void onResponse(Call<ChildGetAndUpdateProfileModel> call, Response<ChildGetAndUpdateProfileModel> response) {
                        CommonUtil.dismissProgress();
                        if (response.body().getSuccess().equalsIgnoreCase("1")) {
                            Toast.makeText(activity, response.body().getMessage() + "", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, response.body().getMessage() + "", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ChildGetAndUpdateProfileModel> call, Throwable t) {
                        CommonUtil.dismissProgress();
                        Log.d("onFailure: ", t.toString());
                        if (t.toString().contains("java.io.FileNotFoundException")) {
                            Toast.makeText(activity, "Change Image then Update", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, t + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                Toast.makeText(activity, "Network Issue", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void GetProfile() {
        if (CommonUtil.isNetworkConnected(activity)) {
            // CommonUtil.showProgress(activity,"Please wait...");
            Api api = ApiClient.apiclient().create(Api.class);
            Call<ChildGetAndUpdateProfileModel> call = api.ChildProfile(Device_id);
            call.enqueue(new Callback<ChildGetAndUpdateProfileModel>() {
                @Override
                public void onResponse(Call<ChildGetAndUpdateProfileModel> call, Response<ChildGetAndUpdateProfileModel> response) {
                    if (response.body().getSuccess().equalsIgnoreCase("1")) {
                        //   CommonUtil.dismissProgress();

                        if (!response.body().getDetails().getImage().equalsIgnoreCase("")) {
                            // CommonUtil.SaveImgPath(activity,response.body().getDetails().getImage());
                            Picasso.get().load(response.body().getDetails().getImage()).into(ChildImage);
                        }
                        ChildName.setText(response.body().getDetails().getName());
                        ChildMail.setText(response.body().getDetails().getEmail());
                        ChildNumber.setText(response.body().getDetails().getPhone());
                        dateOfBirth.setText(response.body().getDetails().getDob());
                        if (response.body().getDetails().getGender().equalsIgnoreCase("Male")) {
                            Gender.setSelection(1);
                        } else if (response.body().getDetails().getGender().equalsIgnoreCase("Female")) {
                            Gender.setSelection(2);
                        } else if (response.body().getDetails().getGender().equalsIgnoreCase("Other")) {
                            Gender.setSelection(2);
                        } else {
                            Gender.setSelection(0);
                        }
                        RelationShip = response.body().getDetails().getRelationship();

                        startActivity(new Intent(activity,SettingActivity.class));
                        activity.finishAffinity();

                    } else {
                        //  CommonUtil.dismissProgress();
                        Toast.makeText(activity, response.body().getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ChildGetAndUpdateProfileModel> call, Throwable t) {
                    // CommonUtil.dismissProgress();
                    Toast.makeText(activity, t + "", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(activity, "Network Issue", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard(View v) {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

}
