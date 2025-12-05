package com.example.myapplication.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Aplica máscara dinâmica para CPF (###.###.###-##) ou CNPJ (##.###.###/####-##).
 * Decide pelo tamanho dos dígitos inseridos.
 */
public class BrDocumentMask implements TextWatcher {
    private final EditText editText;
    private boolean updating;
    private String old = "";

    public BrDocumentMask(EditText et) { this.editText = et; }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {
        if (updating) return;
        String digits = s.toString().replaceAll("[^0-9]", "");
        String formatted;
        if (digits.length() <= 11) { // CPF
            formatted = formatCpf(digits);
        } else { // CNPJ
            formatted = formatCnpj(digits);
        }
        updating = true;
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        updating = false;
    }

    private String formatCpf(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<d.length() && i<11; i++) {
            if (i==3 || i==6) sb.append('.');
            if (i==9) sb.append('-');
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }
    private String formatCnpj(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<d.length() && i<14; i++) {
            if (i==2 || i==5) sb.append('.');
            if (i==8) sb.append('/');
            if (i==12) sb.append('-');
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    public boolean isValid() {
        String digits = editText.getText().toString().replaceAll("[^0-9]", "");
        return digits.length()==11 || digits.length()==14; // tamanho básico
    }
}

