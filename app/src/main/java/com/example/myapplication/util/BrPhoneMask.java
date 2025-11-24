package com.example.myapplication.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Máscara para telefone brasileiro: (##) #####-#### ou (##) ####-#### conforme tamanho.
 */
public class BrPhoneMask implements TextWatcher {
    private final EditText editText;
    private boolean updating;

    public BrPhoneMask(EditText et) { this.editText = et; }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {
        if (updating) return;
        String digits = s.toString().replaceAll("[^0-9]", "");
        String formatted = format(digits);
        updating = true;
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        updating = false;
    }

    private String format(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<d.length() && i<11; i++) {
            if (i==0) sb.append('(');
            if (i==2) sb.append(')').append(' ');
            if ((d.length()>10 && i==7) || (d.length()<=10 && i==6)) sb.append('-');
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    public boolean isValid() {
        String digits = editText.getText().toString().replaceAll("[^0-9]", "");
        // celular 11 digitos ou fixo 10 digitos
        return digits.length()==10 || digits.length()==11;
    }
}

