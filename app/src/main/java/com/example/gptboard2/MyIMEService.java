package com.example.gptboard2;

import android.annotation.SuppressLint;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyIMEService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView keyboardView;
    private Keyboard keyboard;
    private LinearLayout inputView;

    private ImageView companyLogo;
    private EditText editText;
    private Keyboard englishKeyboard;
    private boolean isKoreanKeyboard = true;
    private boolean isSpecialKeyboard = false;
    private boolean isUppercaseKeyboard = false;

    private HangulAutomata hangulAutomata;

    private ChatUI chatUI;


    @Override
    public void onCreate() {
        super.onCreate();
        hangulAutomata = new HangulAutomata();
        chatUI = new ChatUI(this);
    }

    @Override
    public View onCreateInputView() {
        View inputView = getLayoutInflater().inflate(R.layout.keyboard_view, null);


        keyboardView = inputView.findViewById(R.id.keyboard_view);
        keyboard = new Keyboard(this, R.xml.korean_keyboard);
        englishKeyboard = new Keyboard(this, R.xml.english_keyboard);

        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        companyLogo = inputView.findViewById(R.id.company_logo);
        editText = inputView.findViewById(R.id.gpt_edit_text);

        // Set EditText to not focusable initially
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);


        // Set OnClickListener for the company logo
        companyLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle EditText focusability
                boolean isFocusable = editText.isFocusable();
                editText.setFocusable(!isFocusable);
                editText.setFocusableInTouchMode(!isFocusable);
                //로고 버튼을 누른 경우, 엔터키가 검색키로 변경된다.
                if (!isFocusable) {
                    editText.requestFocus();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        keyboardView.getKeyboard().getKeys().stream().filter(key -> key.codes[0] == 10).findFirst().ifPresent(key -> {
                            key.label = null;
                            key.codes[0] = -6; // Change the key code from 10 to -6
                            key.icon = getResources().getDrawable(R.drawable.ic_search); // Replace "R.drawable.ic_search" with your search icon resource
                            key.iconPreview = getResources().getDrawable(R.drawable.ic_search); // Replace "R.drawable.blue_key_background" with your blue key background resource
                        });
                    }
                    keyboardView.invalidateAllKeys();

                // 로고 버튼을 다시 누르면, 검색 키가 다시 엔터키로 변경된다.
                } else {
                    editText.clearFocus();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        keyboardView.getKeyboard().getKeys().stream().filter(key -> key.codes[0] == -6).findFirst().ifPresent(key -> {
                            key.label = "ENT";
                            key.codes[0] = 10; // Change the key code back to 10
                            key.icon = null;
                            key.iconPreview = getResources().getDrawable(R.drawable.default_key_background); // Replace default_key_background with your default key background
                        });
                    }
                    keyboardView.invalidateAllKeys();
                }
            }
        });

        return inputView;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }

//
//        if (isKoreanKeyboard) {
//            String hangulChar = hangulAutomata.composeHangul(primaryCode);
//
//            if (!hangulChar.isEmpty()) {
//                inputConnection.commitText(hangulChar, 1);
//                updateEditText();
//                return;
//            }
//        }

        if (editText.isFocusable()) {
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    CharSequence selectedText = inputConnection.getSelectedText(0);
                    if (selectedText != null && selectedText.length() > 0) {
                        inputConnection.commitText("", 1);
                    } else {
                        inputConnection.deleteSurroundingText(1, 0);
                    }
                    updateEditText();
                    break;
                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    break;

                case -6: // Search key
                    try {
                        // 검색 키를 누른 경우, inputText에 타이핑되어있는 텍스트를 넣는다.
                        if (editText.isFocusable()) {
                            String inputText = editText.getText().toString();

                            // Inflate user_chat_bubble and set the inputText to the TextView
                            //inputText에 저장된 텍스트를 말풍선(textview)에 넣는다.
                            View userChatBubble = getLayoutInflater().inflate(R.layout.user_chat_bubble, null);
                            TextView userChatText = userChatBubble.findViewById(R.id.user_chat_text);
                            userChatText.setText(inputText);

//                            // Add the userChatBubble to the chat_bubble_container
//                            LinearLayout chatBubbleContainer = chatUI.getChatBubbleContainer();
//                            chatBubbleContainer.addView(userChatBubble);

                            // Save the inputText as needed (e.g., store it in a variable or send it to another activity)
                            //switchToChatUI();
//
//                            LinearLayout allKeyboardContainer = (LinearLayout) inputView.findViewById(R.id.all_keyboard_container);
//                            allKeyboardContainer.removeView(inputView.findViewById(R.id.keyboard_view));
//
//                            View replacementLayout = getLayoutInflater().inflate(R.layout.chat_ui, null);
//                            allKeyboardContainer.addView(replacementLayout);
                            editText.setText("");

                            View chatUIView = LayoutInflater.from(this).inflate(R.layout.chat_ui, null);
                            chatUI.initializeChatUIComponents(chatUIView);
                            inputView.removeAllViews();
                            inputView.addView(chatUIView);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case -3: // Switch between Korean and English layout
                    isKoreanKeyboard = !isKoreanKeyboard;
                    if (isKoreanKeyboard) {
                        keyboard = new Keyboard(this, R.xml.korean_keyboard);
                    } else {
                        keyboard = new Keyboard(this, R.xml.english_keyboard);
                    }
                    keyboardView.setKeyboard(keyboard);
                    break;

                case -2: // Switch between special characters layout
                    isSpecialKeyboard = !isSpecialKeyboard;
                    if (isSpecialKeyboard) {
                        keyboard = new Keyboard(this, R.xml.special_keyboard);
                    } else {
                        keyboard = isKoreanKeyboard ? new Keyboard(this, R.xml.korean_keyboard) : new Keyboard(this, R.xml.english_keyboard);
                    }
                    keyboardView.setKeyboard(keyboard);
                    break;

                case -1: // Switch between uppercase and normal layouts
                    isUppercaseKeyboard = !isUppercaseKeyboard;
                    if (isUppercaseKeyboard) {
                        if (isSpecialKeyboard) {
                            keyboard = new Keyboard(this, R.xml.special_upper_keyboard);
                        } else {
                            keyboard = isKoreanKeyboard ? new Keyboard(this, R.xml.korean_upper_keyboard) : new Keyboard(this, R.xml.english_upper_keyboard);
                        }
                    } else {
                        if (isSpecialKeyboard) {
                            keyboard = new Keyboard(this, R.xml.special_keyboard);
                        } else {
                            keyboard = isKoreanKeyboard ? new Keyboard(this, R.xml.korean_keyboard) : new Keyboard(this, R.xml.english_keyboard);
                        }
                    }
                    keyboardView.setKeyboard(keyboard);
                    break;

                default:
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && isUppercaseKeyboard) {
                        code = Character.toUpperCase(code);
                    }
                    inputConnection.commitText(String.valueOf(code), 1);
                    updateEditText();
            }
        }
    }
    private void switchToChatUI() {
        View chatUIView = getLayoutInflater().inflate(R.layout.chat_ui, null);
        chatUI.initializeChatUIComponents(chatUIView);
        setInputView(chatUIView);
    }


    public void switchToKeyboardUI() {
        View keyboardUIView = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        setInputView(keyboardUIView);
        onCreateInputView(); // This line will re-initialize the keyboard UI
    }

    @SuppressLint("SetTextI18n")
    private void updateEditText() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }

        CharSequence textBeforeCursor = inputConnection.getTextBeforeCursor(1000, 0);
        CharSequence textAfterCursor = inputConnection.getTextAfterCursor(1000, 0);
        editText.setText(textBeforeCursor.toString() + textAfterCursor.toString());
        editText.setSelection(textBeforeCursor.length());
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
