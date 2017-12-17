package com.axiom.cookmate.activity;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.axiom.cookmate.ErrorDialogFragment;
import com.axiom.cookmate.FirebaseAnalyticsUtils;
import com.axiom.cookmate.R;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.RecipeContract.MyRecipeEntry;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateRecipeActivity extends AppCompatActivity {

    @BindView(R.id.add_recipe_pic_button)
    ImageButton mAddRecipeImageButton;

    @BindView(R.id.create_ingredient_list_layout)
    LinearLayout mCreateIngredientListLayout;

    @BindView(R.id.create_instruction_list_layout)
    LinearLayout mCreateInstructionListLayout;

    @BindView(R.id.create_edit_text_ingredient)
    EditText mCreateIngredientEditText;

    @BindView(R.id.create_edit_text_instruction)
    EditText mCreateInstructionEditText;

    @BindView(R.id.add_ingredient_button)
    ImageView mAddIngredientButton;

    @BindView(R.id.add_instruction_button)
    ImageView mAddInstructionButton;

    @BindView(R.id.create_recipe_image_view)
    ImageView mCreateRecipeImageView;

    @BindView(R.id.title_edit_text)
    EditText mRecipeTitleEditText;

    private String mRecipeTitle;
    private String mPicturePath;
    private EditText mTouchedEditText;

    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String EVENT_CREATE_RECIPE = "create_recipe";
    private static final String EVENT_VOICE_INPUT_CLICKED = "voice_input_clicked";

    private static int REQUEST_CAMERA = 1;
    private static int SELECT_FILE = 2;
    private static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private final int REQ_CODE_SPEECH_INPUT = 500;

    private ArrayList<String> mIngredientList = null;
    private ArrayList<String> mInstructionList = null;

    DatabaseReference mMyRecipeNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_recipe_layout);
        ButterKnife.bind(this);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String title = getString(R.string.create_recipe_string);
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_create_recipe);
        setSupportActionBar(toolbar);

        if (AccountUtils.getUserLogin(this)) {
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");
            String userId = AccountUtils.getFirebaseUserId(this);
            DatabaseReference firebaseUserNode = mFirebaseDBRef.child(userId);
            mMyRecipeNode = firebaseUserNode.child("my_recipe_list");
        }
        // Use this edit text to store current focused edittext
        // and enter text returned from mic
        mTouchedEditText = null;
        mRecipeTitleEditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                mTouchedEditText = (EditText) view;
                return false;
            }
        });

        mAddRecipeImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickAnImage();
            }
        });

        mAddIngredientButton = (ImageView) findViewById(R.id.add_ingredient_button);
        mAddIngredientButton.setTag(ADD);
        mAddIngredientButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAddIngredientClick(mAddIngredientButton);
            }
        });

        mAddInstructionButton = (ImageView) findViewById(R.id.add_instruction_button);
        mAddInstructionButton.setTag(ADD);
        mAddInstructionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAddInstructionClick(mAddInstructionButton);
            }
        });

        mCreateIngredientEditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                mTouchedEditText = (EditText) view;
                return false;
            }
        });

        mCreateInstructionEditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                mTouchedEditText = (EditText) view;
                return false;
            }
        });

        if (savedInstanceState != null) {
            mRecipeTitleEditText.setText(savedInstanceState.getString(Constants.RECIPE_TITLE, ""));
            mPicturePath = savedInstanceState.getString(Constants.PICTURE_PATH, null);
            loadImageFromStorage(mPicturePath);

            ArrayList<String> ingredientList = savedInstanceState.getStringArrayList(Constants.INGREDIENT_LIST);
            mIngredientList = ingredientList;
            if (ingredientList != null && ingredientList.size() > 0) {
                mCreateIngredientEditText.setText(ingredientList.get(0));
                if (ingredientList.size() > 1) {
                    setRemoveRowImage(mAddIngredientButton);
                    for (int i = 1; i < ingredientList.size(); i++) {
                        View ingredientRow = createIngredientRow();
                        mCreateIngredientListLayout.addView(ingredientRow);
                        if (i + 1 < ingredientList.size()) {
                            ImageView imageView = (ImageView) ((LinearLayout) ingredientRow).getChildAt(1);
                            setRemoveRowImage(imageView);
                        }
                    }
                }
            }

            ArrayList<String> instructionList = savedInstanceState.getStringArrayList(Constants.INSTRUCTION_LIST);
            mInstructionList = instructionList;
            if (instructionList != null && instructionList.size() > 0) {
                mCreateInstructionEditText.setText(instructionList.get(0));
                if (instructionList.size() > 1) {
                    setRemoveRowImage(mAddInstructionButton);
                    for (int i = 1; i < instructionList.size(); i++) {
                        View instructionRow = createInstructionRow();
                        mCreateInstructionListLayout.addView(instructionRow);
                        if (i + 1 < instructionList.size()) {
                            ImageView imageView = (ImageView) ((LinearLayout) instructionRow).getChildAt(1);
                            setRemoveRowImage(imageView);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIngredientList != null) {
            for (int i = 1; i < mCreateIngredientListLayout.getChildCount(); i++) {
                LinearLayout linearLayout1 = (LinearLayout) mCreateIngredientListLayout.getChildAt(i);
                EditText ingredientEditText1 = (EditText) linearLayout1.getChildAt(0);
                ingredientEditText1.setText(mIngredientList.get(i));
            }
        }
        if (mInstructionList != null) {
            for (int i = 1; i < mCreateInstructionListLayout.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) mCreateInstructionListLayout.getChildAt(i);
                EditText instructionEditText = (EditText) linearLayout.getChildAt(0);
                instructionEditText.setText(mInstructionList.get(i));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.RECIPE_TITLE, mRecipeTitle);
        outState.putString(Constants.PICTURE_PATH, mPicturePath);
        ArrayList<String> ingredientList = new ArrayList<>();
        for (int i = 0; i < mCreateIngredientListLayout.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) mCreateIngredientListLayout.getChildAt(i);
            EditText ingredientEditText = (EditText) linearLayout.getChildAt(0);
            ingredientList.add(ingredientEditText.getText().toString().trim());
        }
        outState.putStringArrayList(Constants.INGREDIENT_LIST, ingredientList);
        ArrayList<String> instructionList = new ArrayList<>();
        for (int i = 0; i < mCreateInstructionListLayout.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) mCreateInstructionListLayout.getChildAt(i);
            EditText instructionEditText = (EditText) linearLayout.getChildAt(0);
            instructionList.add(instructionEditText.getText().toString().trim());
        }
        outState.putStringArrayList(Constants.INSTRUCTION_LIST, instructionList);
    }

    private void onAddIngredientClick(ImageView v) {
        String action = (String) v.getTag();
        //If button is add, inflate a row and attach to layout
        if (action.equals(ADD)) {
            handleAddIngredient(v);
        } else if (action.equals(REMOVE)) {
            mCreateIngredientListLayout.removeView((LinearLayout) (v.getParent()));
        }
    }

    private void handleAddIngredient(ImageView view) {
        View ingredientRow = createIngredientRow();
        mCreateIngredientListLayout.addView(ingredientRow);
        setRemoveRowImage(view);
    }

    private View createIngredientRow() {
        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ingredientRow = layoutInflater.inflate(R.layout.ingredient_row_layout, null);
        EditText ingredientEditText = (EditText)
                ingredientRow.findViewById(R.id.row_create_ingredient_text);
        ImageView ingredientAddButton = (ImageView)
                ingredientRow.findViewById(R.id.row_add_ingredient_view);
        ingredientEditText.requestFocus();
        ingredientEditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                mTouchedEditText = (EditText) view;
                return false;
            }
        });
        ingredientAddButton.setTag(ADD);
        ingredientAddButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAddIngredientClick((ImageView) v);
            }
        });
        return ingredientRow;
    }

    private void setRemoveRowImage(ImageView view) {
        view.setImageResource(R.drawable.ic_remove_button);
        view.setTag(REMOVE);
        view.setContentDescription(getString(R.string.a11y_remove_row_button));
    }

    private void onAddInstructionClick(ImageView v) {
        String action = (String) v.getTag();
        if (action.equals(ADD)) {
            handleAddInstruction(v);
        } else if (action.equals(REMOVE)) {
            mCreateInstructionListLayout.removeView((LinearLayout) (v.getParent()));
        }
    }

    private void handleAddInstruction(ImageView view) {
        View instructionRow = createInstructionRow();
        mCreateInstructionListLayout.addView(instructionRow);
        setRemoveRowImage(view);
    }

    private View createInstructionRow() {
        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View instructionRow = layoutInflater.inflate(R.layout.instruction_row_layout, null);
        EditText instructionEditText = (EditText)
                instructionRow.findViewById(R.id.row_create_instruction_text);
        ImageView instructionAddButton = (ImageView)
                instructionRow.findViewById(R.id.row_add_instruction_view);
        instructionEditText.requestFocus();
        instructionEditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                mTouchedEditText = (EditText) view;
                return false;
            }
        });
        instructionAddButton.setTag(ADD);
        instructionAddButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAddInstructionClick((ImageView) v);
            }
        });
        return instructionRow;
    }

    private void pickAnImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        final CharSequence[] items = {
                getResources().getString(R.string.take_pic_option),
                getResources().getString(R.string.choose_gallery_option),
                getResources().getString(R.string.pic_dialog_cancel_option)};
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateRecipeActivity.this);
        builder.setTitle(getResources().getString(R.string.add_pic_dialog));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getResources().getString(R.string.take_pic_option))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals(getResources().getString(R.string.choose_gallery_option))) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.gallery_chooser_title)), SELECT_FILE);
                } else if (items[item].equals(getResources().getString(R.string.pic_dialog_cancel_option))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                String absolutePath = saveToInternalStorage(photo);
                mPicturePath = absolutePath;
                loadImageFromStorage(absolutePath);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImage = data.getData();
                String[] projection = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        projection, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                Bitmap bm = BitmapFactory.decodeFile(picturePath, btmapOptions);
                String absolutePath = saveToInternalStorage(bm);

                mPicturePath = absolutePath;
                loadImageFromStorage(absolutePath);
            } else if (requestCode == REQ_CODE_SPEECH_INPUT && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (mTouchedEditText != null) {
                    mTouchedEditText.setText(result.get(0));
                }
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        String pictureName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        File myPath = new File(directory, pictureName);
        mPicturePath = myPath.getAbsolutePath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return myPath.getAbsolutePath();
    }

    // Load recipe image from storage and set it to ImageView.
    private void loadImageFromStorage(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                File f = new File(path);
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                mCreateRecipeImageView.setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void promptSpeechInput() {
        Bundle b = new Bundle();
        b.putString(EVENT_VOICE_INPUT_CLICKED, getString(R.string.event_voice_input_clicked));
        FirebaseAnalyticsUtils.reportVoiceInputClicked(this, b);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.speech_not_supported_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecipe() {
        // Error Dialog Fragment for different user input.
        String[] errorStrings = checkInput();
        if (errorStrings != null) {
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
            Bundle b = new Bundle();
            b.putString(Constants.ERROR_DIALOG_TITLE, errorStrings[0]);
            b.putString(Constants.ERROR_DIALOG_BODY, errorStrings[1]);
            errorDialogFragment.setArguments(b);
            errorDialogFragment.show(getFragmentManager(), Constants.ALERT_DIALOG_TAG);
        } else {
            // Get strings from all edit texts and use StringBuilder to join them and store in database.
            StringBuilder ingredient = new StringBuilder();
            for (int i = 0; i < mCreateIngredientListLayout.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) mCreateIngredientListLayout.getChildAt(i);
                EditText ingredientEditText = (EditText) linearLayout.getChildAt(0);
                ingredient.append(ingredientEditText.getText().toString().trim());
                ingredient.append(";");
            }

            StringBuilder instruction = new StringBuilder();
            for (int i = 0; i < mCreateInstructionListLayout.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) mCreateInstructionListLayout.getChildAt(i);
                EditText instructionEditText = (EditText) linearLayout.getChildAt(0);
                instruction.append(instructionEditText.getText().toString().trim());
                instruction.append(";");
            }

            // Saving the recipe to the database.
            ContentValues values = new ContentValues();
            values.put(MyRecipeEntry.COLUMN_IMAGE, mPicturePath);
            values.put(MyRecipeEntry.COLUMN_RECIPE_NAME, mRecipeTitle);
            values.put(MyRecipeEntry.COLUMN_INSTRUCTION, instruction.toString());
            values.put(MyRecipeEntry.COLUMN_INGREDIENTS, ingredient.toString());
            Uri newUri = getContentResolver().insert(MyRecipeEntry.CONTENT_URI, values);

            // Display a toast after database is updated.
            Toast.makeText(this, getString(R.string.saved_recipe_toast, mRecipeTitle), Toast.LENGTH_SHORT).show();

            String userId = newUri.getLastPathSegment();
            ArrayList<String> ingredientList = new ArrayList<>();
            ArrayList<String> instructionList = new ArrayList<>();
            String ingredientString = ingredient.toString();
            StringTokenizer st1 = new StringTokenizer(ingredientString, ";");
            while (st1.hasMoreTokens()) {
                ingredientList.add(st1.nextToken());
            }
            String instructionString = instruction.toString();
            instructionString = instructionString.substring(0, instruction.length() - 1);
            StringTokenizer st2 = new StringTokenizer(instructionString, ";");
            while (st2.hasMoreTokens()) {
                instructionList.add(st2.nextToken());
            }

            if (AccountUtils.getUserLogin(this)) {
                MyRecipe myRecipe = new MyRecipe(userId, mRecipeTitle, mPicturePath, instructionList, ingredientList);
                // Sync: Add to realtime DB
                mMyRecipeNode.child(newUri.getLastPathSegment()).setValue(myRecipe);
            }
            // Create Recipe Event Analytics
            Bundle b = new Bundle();
            b.putString(EVENT_CREATE_RECIPE, mRecipeTitle);
            FirebaseAnalyticsUtils.reportCreateRecipeEvent(this, b);
            finish();
        }
    }

    /* Check for user input if atleast one ingredient and instruction are entered
       and pop up error dialogs appropriately. */
    private String[] checkInput() {
        String[] errorStrings = new String[2];
        mRecipeTitle = mRecipeTitleEditText.getText().toString().trim();
        String firstIngredientString = mCreateIngredientEditText.getText().toString().trim();
        String firstInstructionString = mCreateInstructionEditText.getText().toString().trim();

        if (mRecipeTitle.isEmpty() || mRecipeTitle.matches("")) {
            errorStrings[0] = getString(R.string.recipe_name_error_title);
            errorStrings[1] = getString(R.string.recipe_name_error_body);
        } else if (firstIngredientString.isEmpty() || firstIngredientString.matches("")) {
            errorStrings[0] = getString(R.string.recipe_ingredient_error_title);
            errorStrings[1] = getString(R.string.recipe_ingredient_error_body);
        } else if (firstInstructionString.isEmpty() || firstInstructionString.matches("")) {
            errorStrings[0] = getString(R.string.recipe_instruction_error_title);
            errorStrings[1] = getString(R.string.recipe_instruction_error_body);
        } else {
            return null;
        }
        return errorStrings;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Implementation for user clicks on different menu items.
        switch (item.getItemId()) {
            case R.id.action_mic:
                promptSpeechInput();
                break;
            case R.id.action_save:
                saveRecipe();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
