package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

public class SpecsActivity extends AppCompatActivity {

    private MachineHelper thisMachineHelper = MainActivity.getMachineHelper();

    private SharedPreferences thisPrefs = MainActivity.getPrefs();

    private int machineID = -1;

    private int[] categoryStartEnd = {};

    private int machineIDPosition = 0;

    private boolean startup = true;

    private MediaPlayer startupSound = null;

    private MediaPlayer deathSound = null;

    private View mainView = null;

    private ScrollView mainScrollView = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        try {
            final Intent intent = getIntent();
            machineID = intent.getIntExtra("machineID", -1);
            categoryStartEnd = intent.getIntArrayExtra("thisCategory");
            // Find the current position.
            for (int i = 0; i < categoryStartEnd.length; i++) {
                if (categoryStartEnd[i] == machineID) {
                    machineIDPosition = i;
                    break;
                }
            }
            mainView = findViewById(R.id.mainView);
            mainScrollView = findViewById(R.id.mainScrollView);
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    private void initialize() {
        try {
            if (machineID == -1) {
                throw new IllegalArgumentException();
            }
            initSpecs();
            initImage();
            initLinks();
            if (thisPrefs.getBoolean("isUseNavButtons", false)) {
                initButtons();
            }
            if (thisPrefs.getBoolean("isUseGestures", true)) {
                initGestures();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
        Log.i("SpecsInitialize", "Machine ID " + machineID);
    }

    private void release() {
        try {
            if (startupSound != null && startupSound.isPlaying()) {
                startupSound.stop();
                Log.i("releaseSound", "Startup sound stopped");
            }
            if (deathSound != null && deathSound.isPlaying()) {
                deathSound.stop();
                Log.i("releaseSound", "Death sound stopped");
            }
            if (startupSound != null) {
                startupSound.release();
                Log.i("releaseSound", "Startup sound released");
            }
            if (deathSound != null) {
                deathSound.release();
                Log.i("releaseSound", "Death sound released");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initSpecs() {
        TextView name = findViewById(R.id.nameText);
        TextView type = findViewById(R.id.typeText);
        TextView processor = findViewById(R.id.processorText);
        TextView maxram = findViewById(R.id.maxramText);
        TextView year = findViewById(R.id.yearText);
        TextView model = findViewById(R.id.modelText);

        this.setTitle(thisMachineHelper.getName(machineID));
        name.setText(thisMachineHelper.getName(machineID));
        type.setText(thisMachineHelper.getType(machineID));
        processor.setText(thisMachineHelper.getProcessor(machineID));
        maxram.setText(thisMachineHelper.getMaxRam(machineID));
        year.setText(thisMachineHelper.getYear(machineID));
        model.setText(thisMachineHelper.getModel(machineID));

        /*
           Processor Images dynaLoad.

           (1) Try getting type image. If type image is present, will only load from it.
           (2) Try getting specific image. Will load if specific image(s) is/are present.
           (3) No action. The case is not applicable for both loading process.
         */
        LinearLayout processorAllImagesContainer = findViewById(R.id.processorAllImages);
        ImageView processorTypeImage = findViewById(R.id.processorTypeImage);
        LinearLayout processorImages = findViewById(R.id.processorImageLayout);
        // Default states are all hidden.
        processorAllImagesContainer.setVisibility(View.GONE);
        processorTypeImage.setVisibility(View.GONE);
        processorImages.setVisibility(View.GONE);

        int processorTypeImageRes = thisMachineHelper.getProcessorTypeImage(machineID);
        if (processorTypeImageRes == 0) {
            // Not applicable for type image loading, trying specific image.
            int[][] processorImageRes = thisMachineHelper.getProcessorImage(machineID);
            if (processorImageRes[0][0] != 0) {
                // Got specific images. Now loading.
                processorImages.setVisibility(View.VISIBLE);
                processorAllImagesContainer.setVisibility(View.VISIBLE);
                // Clear all existing children.
                processorImages.removeAllViews();
                for (int[] processorImageResGroup : processorImageRes) {
                    for (final int thisProcessorImageRes : processorImageResGroup) {
                        View imageChunk = getLayoutInflater().inflate(R.layout.chunk_processor_image, null);
                        View spaceChunk = getLayoutInflater().inflate(R.layout.chunk_processor_image_space, null);
                        ImageView thisProcessorImage = imageChunk.findViewById(R.id.processorImage);
                        thisProcessorImage.setImageResource(thisProcessorImageRes);
                        processorImages.addView(imageChunk);
                        processorImages.addView(spaceChunk);
                    }
                }
                // Remove the last space.
                processorImages.removeViewAt(processorImages.getChildCount() - 1);
            }
        } else {
            // Got type image. Now loading.
            processorTypeImage.setVisibility(View.VISIBLE);
            processorAllImagesContainer.setVisibility(View.VISIBLE);
            processorTypeImage.setImageResource(processorTypeImageRes);
        }
    }

    private void initImage() {
        // Init image
        ImageView image = findViewById(R.id.pic);
        File imageFile = thisMachineHelper.getPicture(machineID);
        if (imageFile.exists()) {
            Log.i("SpecsAct", "Image exists");
            image.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
        }
        imageFile.delete();

        // Init startup and death sound
        int[] sound = thisMachineHelper.getSound(machineID);
        int startupID = sound[0];
        int deathID = sound[1];
        TextView informationLabel = findViewById(R.id.information);
        if (startupID != 0 && deathID != 0
                && thisPrefs.getBoolean("isPlayDeathSound", true)) {
            // Startup sound exists, death sound exists, and user prefers both
            informationLabel.setText(getResources().getString(R.string.information_specs_full));
            startupSound = MediaPlayer.create(this, startupID);
            deathSound = MediaPlayer.create(this, deathID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    if (!startupSound.isPlaying() && !deathSound.isPlaying()) {
                        if (startup) {
                            startupSound.start();
                            startup = false;
                        } else {
                            deathSound.start();
                            startup = true;
                        }
                    }
                }
            });
            image.setClickable(true);
            informationLabel.setVisibility(View.VISIBLE);
            Log.i("InitSound", "Startup and death sound loaded");
        } else if (startupID != 0) {
            // Startup sound exists, death sound not exist
            // Fix IllegalStateException
            deathSound = null;
            informationLabel.setText(getResources().getString(R.string.information_specs_no_death));
            startupSound = MediaPlayer.create(this, startupID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    startupSound.start();
                }
            });
            image.setClickable(true);
            informationLabel.setVisibility(View.VISIBLE);
            Log.i("InitSound", "Startup sound loaded");
        } else {
            // Exception for PowerBook DuoDock...
            // Fix IllegalStateException
            startupSound = null;
            deathSound = null;
            image.setOnClickListener(null);
            image.setClickable(false);
            informationLabel.setVisibility(View.GONE);
            Log.i("InitSound", "Startup and death sound do not exist");
        }
    }

    private void initLinks() {
        ImageView link = findViewById(R.id.everymac);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadLinks(thisMachineHelper.getName(machineID), thisMachineHelper.getConfig(machineID));
            }
        });
    }

    // Keep compatible with MainActivity.
    private void loadLinks(final String thisName, final String thisLinks) {
        try {
            if (thisLinks.equals("N")) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.link_not_available), Toast.LENGTH_LONG).show();
                return;
            }
            final String[] linkGroup = thisLinks.split(";");
            if (linkGroup.length == 1) {
                // Only one option, launch EveryMac directly.
                startBrowser(linkGroup[0].split(",")[0], linkGroup[0].split(",")[1]);
            } else {
                AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);
                linkDialog.setTitle(thisName);
                linkDialog.setMessage(getResources().getString(R.string.link_message));
                // Setup each option in dialog.
                View linkChunk = getLayoutInflater().inflate(R.layout.chunk_links, null);
                final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
                for (int i = 0; i < linkGroup.length; i++) {
                    RadioButton linkOption = new RadioButton(this);
                    linkOption.setText(linkGroup[i].split(",")[0]);
                    linkOption.setId(i);
                    if (i == 0) {
                        linkOption.setChecked(true);
                    }
                    linkOptions.addView(linkOption);
                }
                linkDialog.setView(linkChunk);

                // When user tapped confirm or cancel...
                linkDialog.setPositiveButton(this.getResources().getString(R.string.link_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                try {
                                    startBrowser(linkGroup[linkOptions.getCheckedRadioButtonId()]
                                            .split(",")[0], linkGroup[linkOptions.getCheckedRadioButtonId()]
                                            .split(",")[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(),
                                            getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                linkDialog.setNegativeButton(this.getResources().getString(R.string.link_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                // Cancelled.
                            }
                        });
                linkDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("loadLinks", "Link loading failed!!");
        }
    }

    private void startBrowser(final String thisName, final String url) {
        try {
            Intent browser = new Intent(Intent.ACTION_VIEW);
            browser.setData(Uri.parse(url));
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.link_opening) + thisName, Toast.LENGTH_LONG).show();
            startActivity(browser);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initButtons() {
        try {
            Log.i("SpecNavButtons", "Loading");
            View buttonView = findViewById(R.id.buttonView);
            final Button previous = findViewById(R.id.buttonPrevious);
            final Button next = findViewById(R.id.buttonNext);
            // Reset the listener
            previous.setOnClickListener(null);
            next.setOnClickListener(null);
            // GONE by default, let it show up
            buttonView.setVisibility(View.VISIBLE);

            // Previous button.
            if (machineIDPosition == 0) {
                // First one, disable the prev button
                previous.setEnabled(false);
                previous.setText(getResources().getString(R.string.first_one));
            } else {
                previous.setEnabled(true);
                previous.setText(MainActivity.getMachineHelper().getName(machineID - 1));
                previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        previous.setEnabled(false);
                        navPrev();
                    }
                });
            }
            // Next button.
            if (machineIDPosition == categoryStartEnd.length - 1) {
                // Last one, disable the next button
                next.setEnabled(false);
                next.setText(getResources().getString(R.string.last_one));
            } else {
                next.setEnabled(true);
                next.setText(MainActivity.getMachineHelper().getName(machineID + 1));
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        next.setEnabled(false);
                        navNext();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initGestures() {
        Log.i("SpecGestures", "Loading");
        if (machineIDPosition == 0 && machineIDPosition == categoryStartEnd.length - 1) {
            // Can NOT do BOTH
            mainView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.last_one), Toast.LENGTH_LONG).show();
                }
                public void onSwipeLeft() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.first_one), Toast.LENGTH_LONG).show();
                }
            });
            mainScrollView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.last_one), Toast.LENGTH_LONG).show();
                }
                public void onSwipeLeft() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.first_one), Toast.LENGTH_LONG).show();
                }
            });
        } else if (machineIDPosition == 0) {
            // Can only swipe Right (NEXT)
            mainView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    releaseGestures();
                    navNext();
                }
                public void onSwipeLeft() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.first_one), Toast.LENGTH_LONG).show();
                }
            });
            mainScrollView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    releaseGestures();
                    navNext();
                }
                public void onSwipeLeft() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.first_one), Toast.LENGTH_LONG).show();
                }
            });
        } else if (machineIDPosition == categoryStartEnd.length - 1) {
            // Can only swipe Left (PREV)
            mainView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.last_one), Toast.LENGTH_LONG).show();
                }
                public void onSwipeLeft() {
                    releaseGestures();
                    navPrev();
                }
            });
            mainScrollView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.last_one), Toast.LENGTH_LONG).show();
                }
                public void onSwipeLeft() {
                    releaseGestures();
                    navPrev();
                }
            });
        } else {
            // Can do BOTH
            mainView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    releaseGestures();
                    navNext();
                }
                public void onSwipeLeft() {
                    releaseGestures();
                    navPrev();
                }
            });
            mainScrollView.setOnTouchListener(new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    releaseGestures();
                    navNext();
                }
                public void onSwipeLeft() {
                    releaseGestures();
                    navPrev();
                }
            });
        }
    }

    private void releaseGestures() {
        mainView.setOnTouchListener(null);
        mainScrollView.setOnTouchListener(null);
        Log.i("SpecGestures", "Released");
    }

    private void navPrev() {
        machineIDPosition--;
        refresh();
    }

    private void navNext() {
        machineIDPosition++;
        refresh();
    }

    private void refresh() {
        machineID = categoryStartEnd[machineIDPosition];
        if (MainActivity.getPrefs().getBoolean("isQuickNav", false)) {
            // Old method - not creating a new Activity
            release();
            startup = true;
            initialize();
            mainView.invalidate();
        } else {
            // New method
            Intent newMachine = new Intent(SpecsActivity.this, SpecsActivity.class);
            newMachine.putExtra("machineID", machineID);
            newMachine.putExtra("thisCategory", categoryStartEnd);
            startActivity(newMachine);
            finish();
        }
    }
}
