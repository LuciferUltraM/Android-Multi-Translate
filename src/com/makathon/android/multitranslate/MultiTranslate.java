package com.makathon.android.multitranslate;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class MultiTranslate extends Activity {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private ClipboardManager clipBoard;
	private EditText edtWord;
	private ImageButton btnTranslate;
	private ImageButton btnCopy;
	private ImageButton btnSpeech;
	private Spinner spnLangFrom;
	private Spinner spnLangTo;
	// private ListView lvResult;
	private ArrayAdapter<String> resultAdapter;
	private ArrayAdapter<CharSequence> langFromAdapter;
	private ArrayAdapter<CharSequence> langToAdapter;

	private EditText edtResult;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// ClipboardManager
		clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		// get reference to the UIs
		edtWord = (EditText) findViewById(R.id.edtWord);
		btnTranslate = (ImageButton) findViewById(R.id.btnTranslate);
		btnCopy = (ImageButton) findViewById(R.id.btnCopy);
		btnSpeech = (ImageButton) findViewById(R.id.btnSpeech);
		spnLangFrom = (Spinner) findViewById(R.id.spnLangFrom);
		spnLangTo = (Spinner) findViewById(R.id.spnLangTo);
		// lvResult = (ListView) findViewById(R.id.lvResult);

		edtResult = (EditText) findViewById(R.id.edtResult);
		resultAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		// lvResult.setAdapter(resultAdapter);

		langFromAdapter = ArrayAdapter.createFromResource(this,
				R.array.language_from, android.R.layout.simple_spinner_item);
		langFromAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnLangFrom.setAdapter(langFromAdapter);

		langToAdapter = ArrayAdapter.createFromResource(this, R.array.language,
				android.R.layout.simple_spinner_item);
		langToAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnLangTo.setAdapter(langToAdapter);

		Translate.setHttpReferrer("http://www.google.com");

		btnTranslate.setOnClickListener(clickTranslate);
		edtWord.setOnKeyListener(keyTranslate);
		btnCopy.setOnClickListener(clickCopy);

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			// It has Voice Recognizer installed on this device
			btnSpeech.setOnClickListener(clickSpeech);
		} else {
			// Unfortunately, Voice Recognizer not installed
			btnSpeech.setEnabled(false);
		}

		spnLangFrom.setOnItemSelectedListener(selectItemLang);
		spnLangTo.setOnItemSelectedListener(selectItemLang);
	}

	private void startTranslate() {
		String word = edtWord.getText().toString();
		if (word.length() > 0) {
			try {
				String from = spnLangFrom.getItemAtPosition(
						spnLangFrom.getSelectedItemPosition()).toString();
				String to = spnLangTo.getItemAtPosition(
						spnLangTo.getSelectedItemPosition()).toString();
				Language langFrom = ConvertStringToLanguage(from);
				Language langTo = ConvertStringToLanguage(to);
				String trans = Translate.execute(edtWord.getText().toString(),
						langFrom, langTo);
				resultAdapter.clear();
				resultAdapter.insert(trans, 0);
				resultAdapter.notifyDataSetChanged();
				edtResult.setText(trans);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private Language ConvertStringToLanguage(String langugeFull) {
		Language language = null;
		if (langugeFull.compareTo("Auto Detect") == 0) {
			language = Language.AUTO_DETECT;
		} else if (langugeFull.compareTo("English") == 0) {
			language = Language.ENGLISH;
		} else if (langugeFull.compareTo("Thai") == 0) {
			language = Language.THAI;
		} else if (langugeFull.compareTo("French") == 0) {
			language = Language.FRENCH;
		} else if (langugeFull.compareTo("Japanese") == 0) {
			language = Language.JAPANESE;
		} else if (langugeFull.compareTo("Chinese") == 0) {
			language = Language.CHINESE;
		} else if (langugeFull.compareTo("Korean") == 0) {
			language = Language.KOREAN;
		}
		return language;
	}

	private OnClickListener clickTranslate = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startTranslate();
		}
	};
	private OnKeyListener keyTranslate = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
			if (keyCode == KeyEvent.KEYCODE_ENTER
					&& keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				startTranslate();
				return true;
			}
			return false;
		}
	};
	private OnClickListener clickCopy = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String txtCopy = edtResult.getText().toString();
			if (txtCopy.length() > 0) {
				clipBoard.setText(txtCopy);
				Toast.makeText(MultiTranslate.this, "Copy : " + txtCopy,
						Toast.LENGTH_SHORT).show();
			}
		}
	};
	private OnClickListener clickSpeech = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startVoiceRecognitionActivity();
		}
	};

	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String txt = matches.get(0);
			edtWord.setText(txt);
			startTranslate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private OnItemSelectedListener selectItemLang = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			startTranslate();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}
	};
}