package co.realinventor.forblind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import co.realinventor.forblind.Helpers.FriendlyMessage;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhoneNo;
    private String mUid;
    private String sender = "me";
    private SharedPreferences mSharedPreferences;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private ImageView mImageAttachFile;
    private TextToSpeech tts;

    private FloatingActionButton fab_send;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;


    private final int CHAT_ME = 100;
    private final int CHAT_YOU = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mPhoneNo = mFirebaseUser.getPhoneNumber();
            mUid = mFirebaseUser.getUid();
            Log.d(TAG, "Phone no : "+mPhoneNo);
            Log.d(TAG, "UID : "+mUid);
//            if (mFirebaseUser.getPhotoUrl() != null) {
//                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
//            }
        }

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewMsg);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<FriendlyMessage> parser = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot dataSnapshot) {
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(mPhoneNo);
        final FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {


            private String getTextFromFile(String filePath){
                //Get the text file
                File file = new File(filePath);

                //Read text from file
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                    Log.d(TAG,"File read error");
                }

                Log.d(TAG, "Text To speech : "+text.toString());

                return text.toString();

            }


            private void showAudioDialog(String filePath){
                Log.d(TAG, "Text To Speech");
                final String text = getTextFromFile(filePath);

                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR) {
                            tts.setLanguage(Locale.UK);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                            Log.d(TAG, "onInit");
                        }
                    }
                });




                //show some dialog


                //show dialog
//                new AlertDialog.Builder(getApplicationContext())
//                        .setTitle("Delete entry")
//                        .setMessage("Are you sure you want to delete this entry?")
//
//                        // Specifying a listener allows you to take an action before dismissing the dialog.
//                        // The dialog is automatically dismissed when a dialog button is clicked.
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Continue with delete operation
//                            }
//                        })
//
//                        // A null listener allows the button to dismiss the dialog and take no further action.
//                        .setNegativeButton(android.R.string.no, null)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
            }

            @Override
            protected void onBindViewHolder(@NonNull final MessageViewHolder viewHolder, int i, @NonNull FriendlyMessage friendlyMessage) {
                if (friendlyMessage.getText() != null) {
                    //There's a msg, display it
                    Log.d(TAG, "Text not null");
                    viewHolder.contentTextView.setText(friendlyMessage.getText());
                } else if (friendlyMessage.getFileUrl() != null) {
                    Log.d(TAG, "File URL not null");
                    //There's a file to show
                    final String[] fileLoc = {""};
                    viewHolder.linearLayoutMsg.setVisibility(View.GONE);
                    viewHolder.linearLayoutAudio.setVisibility(View.VISIBLE);
                    viewHolder.playButtonAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Play audio button clicked!");
                            if(fileLoc[0].equals(""))
                                Log.d(TAG, "Filename null");
                            else {
                                Log.d(TAG, "Filename : " + fileLoc[0]);
                                Log.d(TAG, "Do conversion");

                                showAudioDialog(fileLoc[0]);
                            }
                        }
                    });

                    String fileUrl = friendlyMessage.getFileUrl();
                    if (fileUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(fileUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Got downloadable url");
                                            String downloadUrl = task.getResult().toString();
//                                            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
//                                            viewHolder.contentTextView.setText(fileName);

                                            //download file

                                            try {
                                                fileLoc[0] = new DownloadFile().execute(downloadUrl).get();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }


                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    }
                    else{
                        //download directly
                        try {
                            fileLoc[0] = new DownloadFile().execute(friendlyMessage.getFileUrl()).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                else {
                    Log.d(TAG, "Else?");
                }


                viewHolder.timeTextView.setText(friendlyMessage.getTimeInString());
//                if (friendlyMessage.getPhotoUrl() == null) {
//                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
//                            R.drawable.ic_account_circle_black_36dp));
//                } else {
//                    Glide.with(MainActivity.this)
//                            .load(friendlyMessage.getPhotoUrl())
//                            .into(viewHolder.messengerImageView);
//                }
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                MessageViewHolder vh;
                if (viewType == CHAT_ME) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_whatsapp_me, parent, false);
                    vh = new MessageViewHolder(v);
                } else {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_whatsapp_telegram_you, parent, false);
                    vh = new MessageViewHolder(v);
                }
                return vh;
            }

            @Override
            public int getItemViewType(int position) {

                return options.getSnapshots().get(position).isFromMe() ? CHAT_ME : CHAT_YOU;
            }
        };



        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        fab_send = findViewById(R.id.btn_send_float);
        mMessageEditText = findViewById(R.id.editTextMsg);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    fab_send.setImageResource(R.drawable.ic_send);
                } else {
                    fab_send.setImageResource(R.drawable.ic_mic);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), sender, FriendlyMessage.getCurrentTime(),
                        null /* no image */);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(mPhoneNo).push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        mImageAttachFile = findViewById(R.id.imgAttachFile);
        mImageAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFileAttachStuff();
            }
        });
    }

    private void doFileAttachStuff(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //No permission
            Log.d(TAG, "No Permission");

            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                            doFileAttachStuff();
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                    }).check();


            return;
        }

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("file/*");
//        startActivityForResult(intent, REQUEST_IMAGE);

        new ChooserDialog(MessageActivity.this)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        if(pathFile != null){
                            onFileChosen(pathFile);
                        }
                    }
                })
                // to handle the back key pressed or clicked outside the dialog:
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Log.d("CANCEL", "CANCEL");
                        dialog.cancel(); // MUST have
                    }
                })
                .build()
                .show();
    }


    private void onFileChosen(File file){
        final Uri uri = Uri.fromFile(file);
        Log.d(TAG, "Uri: " + uri.toString());


        FriendlyMessage tempMessage = new FriendlyMessage(null, sender, FriendlyMessage.getCurrentTime(), LOADING_IMAGE_URL);
        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(mPhoneNo).push()
                .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();
                            StorageReference storageReference =
                                    FirebaseStorage.getInstance()
                                            .getReference(mFirebaseUser.getUid())
                                            .child(key)
                                            .child(uri.getLastPathSegment());

                            putImageInStorage(storageReference, uri, key);
                        } else {
                            Log.w(TAG, "Unable to write message to database.",
                                    databaseError.toException());
                        }
                    }
                });
    }




    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView;
        ImageView statusImageView;
        TextView timeTextView;
        LinearLayout linearLayoutAudio, linearLayoutMsg;
        ImageView playButtonAudio;

        public MessageViewHolder(View v) {
            super(v);
            contentTextView = (TextView) itemView.findViewById(R.id.text_content);
            timeTextView = (TextView) itemView.findViewById(R.id.text_time);
            statusImageView = (ImageView) itemView.findViewById(R.id.img_status);
            linearLayoutAudio = (LinearLayout) itemView.findViewById(R.id.linearLayoutAudio);
            linearLayoutMsg = (LinearLayout) itemView.findViewById(R.id.linearLayoutMsg);
            playButtonAudio = (ImageView) itemView.findViewById(R.id.playButtonAudio);
        }
    }



    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
//
//        if (requestCode == REQUEST_IMAGE) {
//            if (resultCode == RESULT_OK) {
//                if (data != null) {
//                    final Uri uri = data.getData();
//                    Log.d(TAG, "Uri: " + uri.toString());
//
//
//                    FriendlyMessage tempMessage = new FriendlyMessage(null, sender, FriendlyMessage.getCurrentTime(), LOADING_IMAGE_URL);
//                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(mPhoneNo).push()
//                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
//                                @Override
//                                public void onComplete(DatabaseError databaseError,
//                                                       DatabaseReference databaseReference) {
//                                    if (databaseError == null) {
//                                        String key = databaseReference.getKey();
//                                        StorageReference storageReference =
//                                                FirebaseStorage.getInstance()
//                                                        .getReference(mFirebaseUser.getUid())
//                                                        .child(key)
//                                                        .child(uri.getLastPathSegment());
//
//                                        putImageInStorage(storageReference, uri, key);
//                                    } else {
//                                        Log.w(TAG, "Unable to write message to database.",
//                                                databaseError.toException());
//                                    }
//                                }
//                            });
//                }
//            }
//        }
//    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(MessageActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(MessageActivity.this,
                                            new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        FriendlyMessage friendlyMessage =
                                                                new FriendlyMessage(null, sender, FriendlyMessage.getCurrentTime(),
                                                                        task.getResult().toString());
                                                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(mPhoneNo).child(key)
                                                                .setValue(friendlyMessage);
                                                    }
                                                }
                                            });
                        } else {
                            Log.w(TAG, "File upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }



    private String getTimeString(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd_M_yyyy_hh_mm_ss");
        return  sdf.format(Calendar.getInstance().getTime());
    }


    class DownloadFile extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.d(TAG, "AsyncTask started");
            int count;
            String filePath = null;
            try {
                URL url = new URL(urls[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                filePath = Environment.getExternalStorageDirectory().toString()+"/Blindly/texts" + "/"+getTimeString()+".txt";

                // Output stream
                OutputStream output = new FileOutputStream(filePath);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.d(TAG, "AsyncTask Error");
                Log.e("Error: ", e.getMessage());
            }

            Log.d(TAG, "AsyncTask ended");
            Log.d(TAG, "AsyncTask File : "+filePath);

            return filePath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


}
