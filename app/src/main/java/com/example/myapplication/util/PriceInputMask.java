package com.example.myapplication.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * TextWatcher para aplicar máscara de moeda brasileira em um EditText.
 * Internamente mantém apenas dígitos e formata como R$ 1.234,56.
 */
public class PriceInputMask implements TextWatcher {
    private final EditText editText;
    private String current = "";
    private final Locale locale = new Locale("pt", "BR");

    public PriceInputMask(EditText editText) { this.editText = editText; }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {
        if (s.toString().equals(current)) return;
        String digits = s.toString().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            current = "";
            editText.setText("");
            return;
        }
        // Últimos 2 dígitos são centavos
        double parsed = Double.parseDouble(digits) / 100.0;
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        String formatted = nf.format(parsed);
        current = formatted;
        editText.removeTextChangedListener(this);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.addTextChangedListener(this);
    }

    /**
     * Obtém o valor numérico atual no campo.
     */
    public Double getValue() {
        String txt = editText.getText().toString();
        if (txt.isEmpty()) return null;
        // Remove tudo que não seja dígito e divide por 100.
        String digits = txt.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        return Double.parseDouble(digits) / 100.0;
    }
}

