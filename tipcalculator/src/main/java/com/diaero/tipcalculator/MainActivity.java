package com.diaero.tipcalculator;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String TAG = "MainActivity";
        private View rootView;
        private List<View> editTexts = new ArrayList<View>();
        private EditText editTextBill;
        private EditText editTextPeople;
        private EditText editTextTipPercent;
        private TextView editTextTip;
        private TextView editTextTotal;
        private TextView editTextEachBill;
        private TextView editTextEachTip;
        private TextView editTextEachTotal;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Activity activity = getActivity();
                    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);

                    return false;
                }
            });

            editTexts = getViewsByTag((ViewGroup) rootView, "edittable_field");

            setFields();
            initializeEdittableFields();

            TipData data = recalculateFields();
            displayFields(data);

            return rootView;
        }

        private void initializeEdittableFields() {
            for (int i = 0; i < editTexts.size(); i++) {
                EditText editText = (EditText) editTexts.get(i);
                Log.d(TAG, "initialize field: " + editText);

                enterInitialValue(editText);
                addListener(editText);
            }
        }

        private void setFields() {
            editTextBill = (EditText) rootView.findViewById(R.id.editText_bill);
            editTextPeople = (EditText) rootView.findViewById(R.id.editText_people);
            editTextTipPercent = (EditText) rootView.findViewById(R.id.editText_tip_percent);
            editTextTip = (TextView) rootView.findViewById(R.id.editText_tip);
            editTextTotal = (TextView) rootView.findViewById(R.id.editText_total);
            editTextEachBill = (TextView) rootView.findViewById(R.id.editText_each_bill);
            editTextEachTip = (TextView) rootView.findViewById(R.id.editText_each_tip);
            editTextEachTotal = (TextView) rootView.findViewById(R.id.editText_each_total);
        }

        private void enterInitialValue(EditText editText) {
            String initValue = getInitialValue(editText);
            editText.setText(initValue);
            Log.d(TAG, "  initialized " + editText + " => " + initValue);
        }

        private String getInitialValue(EditText editText) {
            switch (editText.getId()) {
                case R.id.editText_bill:
                    return String.format("%.2f", Constants.DEFAULT_BILL_VALUE);
                case R.id.editText_people:
                    return Integer.toString(Constants.DEFAULT_PEOPLE_VALUE);
                case R.id.editText_tip_percent:
                    return Integer.toString(Constants.DEFAULT_TIP_PERCENT_VALUE);
                case R.id.editText_tip:
                    return String.format("%.2f", Constants.DEFAULT_TIP_VALUE);
                case R.id.editText_total:
                    return String.format("%.2f", Constants.DEFAULT_TOTAL_VALUE);
                case R.id.editText_each_bill:
                    return String.format("%.2f", Constants.DEFAULT_EACH_BILL_VALUE);
                case R.id.editText_each_tip:
                    return String.format("%.2f", Constants.DEFAULT_EACH_TIP_VALUE);
                case R.id.editText_each_total:
                    return String.format("%.2f", Constants.DEFAULT_EACH_TOTAL_VALUE);
                default:
                    return "";
            }
        }

        private void addListener(EditText editText) {
            Log.d(TAG, "addListener: " + editText);
            editText.addTextChangedListener(new EditTextWatcher(editText));

            if (editText.getId() == R.id.editText_bill)
                editText.setOnFocusChangeListener(new FocusChangeListener());
        }

        private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
            ArrayList<View> views = new ArrayList<View>();
            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = root.getChildAt(i);
                if (child instanceof ViewGroup) {
                    views.addAll(getViewsByTag((ViewGroup) child, tag));
                }

                final Object tagObj = child.getTag();
                if (tagObj != null && tagObj.equals(tag)) {
                    views.add(child);
                }

            }
            return views;
        }

        private class EditTextWatcher implements TextWatcher {
            private EditText editText;

            public EditTextWatcher(EditText inEditText) {
                editText = inEditText;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "changed: " + editText + " => " + s + ", " + start + ", " + before + ", " + count);

                String inStr = s.toString();

                if (isValidInput(inStr))
                    return;

                if ((editText == editTextPeople) && Integer.parseInt(inStr) < 1) {
                    showToast(getString(R.string.errormsg_people));
                    setTextAndFocus("1");
                } else if ((editText == editTextTipPercent) && Integer.parseInt(inStr) < 0) {
                    showToast(getString(R.string.errormsg_tip_percent));
                    setTextAndFocus("0");
                }

                TipData data = recalculateFields();
                displayFields(data);
            }

            private boolean isValidInput(String inStr) {
                return inStr.length() != 0 && isNumeric(inStr);
            }

            private void setTextAndFocus(String value) {
                editText.setText(value);
                editText.setSelection(0, editText.getText().length());
            }

            private void showToast(String errorMsg) {
                Toast toast = Toast.makeText(rootView.getContext(), errorMsg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            public void afterTextChanged(Editable s) {
            }

            private boolean isNumeric(String str) {
                NumberFormat formatter = NumberFormat.getInstance();
                ParsePosition pos = new ParsePosition(0);
                formatter.parse(str, pos);
                return str.length() == pos.getIndex();
            }
        }

        private class FocusChangeListener implements View.OnFocusChangeListener {
            public FocusChangeListener() {
            }

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    EditText changedView = (EditText) view;
                    cleanText(changedView);
                }
            }

            private void cleanText(EditText view) {
                Editable editable = view.getText();
                String changedString = editable.toString();

                if (view.getInputType() == InputType.TYPE_CLASS_NUMBER) {
                    view.setText(Integer.toString(Integer.parseInt(changedString)));
                } else if (!view.toString().matches("^(\\d+\\.\\d{2})$")) {
                    view.setText(String.format("%.2f", Double.valueOf(changedString)));
                    Selection.setSelection(editable, changedString.length());
                }
            }

        }


        private void displayFields(TipData data) {
            editTextTip.setText(String.format("%.2f", data.tip));
            editTextTotal.setText(String.format("%.2f", data.total));
            editTextEachBill.setText(String.format("%.2f", data.eachBill));
            editTextEachTip.setText(String.format("%.2f", data.eachTip));
            editTextEachTotal.setText(String.format("%.2f", data.eachTotal));
        }

        private TipData recalculateFields() {
            Log.d(TAG, "calculate");

            TipData data = new TipData();

            int people = Integer.parseInt(editTextPeople.getText().toString());
            Log.d(TAG, "people: " + people);
            int tipPercent = Integer.parseInt(editTextTipPercent.getText().toString());
            Log.d(TAG, "tipPercent: " + tipPercent);
            Double bill = Double.valueOf(editTextBill.getText().toString());
            data = Calculator.getInstance().calculateFromBill(bill, people, tipPercent);

            return data;
        }
    }

}
