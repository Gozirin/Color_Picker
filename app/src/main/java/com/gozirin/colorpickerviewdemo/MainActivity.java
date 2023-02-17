
package com.gozirin.colorpickerviewdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gozirin.colorpickerview.AlphaTileView;
import com.gozirin.colorpickerview.ColorEnvelope;
import com.gozirin.colorpickerview.ColorPickerDialog;
import com.gozirin.colorpickerview.ColorPickerView;
import com.gozirin.colorpickerview.listeners.ColorEnvelopeListener;
import com.gozirin.colorpickerview.flag.BubbleFlag;
import com.gozirin.colorpickerview.flag.FlagMode;
import com.gozirin.colorpickerview.sliders.AlphaSlideBar;
import com.gozirin.colorpickerview.sliders.BrightnessSlideBar;
import com.skydoves.colorpickerviewdemo.R;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import java.io.FileNotFoundException;
import java.io.InputStream;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  private ColorPickerView colorPickerView;

  private boolean FLAG_PALETTE = false;
  private boolean FLAG_SELECTOR = false;

  private PowerMenu powerMenu;
  private final OnMenuItemClickListener<PowerMenuItem> powerMenuItemClickListener =
      new OnMenuItemClickListener<>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
          switch (position) {
            case 1:
              palette();
              break;
            case 2:
              paletteFromGallery();
              break;
            case 3:
              selector();
              break;
            case 4:
              dialog();
              break;
          }
          powerMenu.dismiss();
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Timber.plant(new Timber.DebugTree());

    powerMenu = PowerMenuUtils.getPowerMenu(this, this, powerMenuItemClickListener);

    colorPickerView = findViewById(R.id.colorPickerView);
    BubbleFlag bubbleFlag = new BubbleFlag(this);
    bubbleFlag.setFlagMode(FlagMode.FADE);
    colorPickerView.setFlagView(bubbleFlag);
    colorPickerView.setColorListener(
        (ColorEnvelopeListener)
            (envelope, fromUser) -> {
              Timber.d("color: %s", envelope.getHexCode());
              setLayoutColor(envelope);
            });

    // attach alphaSlideBar
    final AlphaSlideBar alphaSlideBar = findViewById(R.id.alphaSlideBar);
    colorPickerView.attachAlphaSlider(alphaSlideBar);

    // attach brightnessSlideBar
    final BrightnessSlideBar brightnessSlideBar = findViewById(R.id.brightnessSlide);
    colorPickerView.attachBrightnessSlider(brightnessSlideBar);
    colorPickerView.setLifecycleOwner(this);
  }

  /**
   * set layout color & textView html code
   *
   * @param envelope ColorEnvelope by ColorEnvelopeListener
   */
  @SuppressLint("SetTextI18n")
  private void setLayoutColor(ColorEnvelope envelope) {
    TextView textView = findViewById(R.id.textView);
    textView.setText("#" + envelope.getHexCode());

    AlphaTileView alphaTileView = findViewById(R.id.alphaTileView);
    alphaTileView.setPaintColor(envelope.getColor());
  }

  /** shows the popup menu for changing options.. */
  public void overflowMenu(View view) {
    powerMenu.showAsAnchorLeftTop(view);
  }

  /** changes palette image using drawable resource. */
  private void palette() {
    if (FLAG_PALETTE) {
      colorPickerView.setHsvPaletteDrawable();
    } else {
      colorPickerView.setPaletteDrawable(ContextCompat.getDrawable(this, R.drawable.palettebar));
    }
    FLAG_PALETTE = !FLAG_PALETTE;
  }

  /** changes palette image from a gallery image. */
  private void paletteFromGallery() {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    startActivityForResult(photoPickerIntent, 1000);
  }

  /** changes selector image using drawable resource. */
  private void selector() {
    if (FLAG_SELECTOR) {
      colorPickerView.setSelectorDrawable(ContextCompat.getDrawable(this, R.drawable.wheel));
    } else {
      colorPickerView.setSelectorDrawable(ContextCompat.getDrawable(this, R.drawable.wheel_dark));
    }
    FLAG_SELECTOR = !FLAG_SELECTOR;
  }

  /** shows ColorPickerDialog */
  private void dialog() {
    ColorPickerDialog.Builder builder =
        new ColorPickerDialog.Builder(this)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("Test")
            .setPositiveButton(
                getString(R.string.confirm),
                (ColorEnvelopeListener) (envelope, fromUser) -> setLayoutColor(envelope))
            .setNegativeButton(
                getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
    builder.getColorPickerView().setFlagView(new BubbleFlag(this));
    builder.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // user choose a picture from gallery
    if (requestCode == 1000 && resultCode == RESULT_OK) {
      try {
        final Uri imageUri = data.getData();
        if (imageUri != null) {
          final InputStream imageStream = getContentResolver().openInputStream(imageUri);
          final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
          Drawable drawable = new BitmapDrawable(getResources(), selectedImage);
          colorPickerView.setPaletteDrawable(drawable);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (powerMenu.isShowing()) {
      powerMenu.dismiss();
    } else {
      super.onBackPressed();
    }
  }
}
