package com.example.mynote.style;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class StyleText {

    CharacterStyle styleBold, styleUnderline, styleItalic, styleNormal;

    public StyleText() {
        styleBold = new StyleSpan(Typeface.BOLD);
        styleUnderline = new UnderlineSpan();
        styleItalic = new StyleSpan(Typeface.ITALIC);
        styleNormal = new StyleSpan(Typeface.NORMAL);
    }

    public void formatText(EditText editText, String style) {

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if ((end - start) != 0) {

            if (style == "bold") {

                Spannable sb = new SpannableStringBuilder(editText.getText());
                sb.setSpan(styleBold, start, end, 0);

                editText.setText(sb);

            }

            if (style == "underline") {

                SpannableStringBuilder sb = new SpannableStringBuilder(editText.getText());
                sb.setSpan(styleUnderline, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                editText.setText(sb);
            }

            if (style == "italic") {

                SpannableStringBuilder sb = new SpannableStringBuilder(editText.getText());
                sb.setSpan(styleItalic, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                editText.setText(sb);
            }
        }

        if (style == "left"){
            editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            SpannableStringBuilder sb = new SpannableStringBuilder(editText.getText());

            editText.setText(sb);
        }

        else if (style == "center"){
            editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            SpannableStringBuilder sb = new SpannableStringBuilder(editText.getText());

            editText.setText(sb);
        }

        else if (style == "right"){
            editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            SpannableStringBuilder sb = new SpannableStringBuilder(editText.getText());

            editText.setText(sb);
        }
    }
}
