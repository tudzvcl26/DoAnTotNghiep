# Manual Windows Vietnamese IME verification

Status: **Native physical Windows Telex/VNI: MANUAL VERIFICATION REQUIRED**.

Automated coverage protects `compositionstart`, `compositionupdate`, `compositionend`, input, blur before composition end, IME Enter (`isComposing`/key code 229), completed Enter, autosave pause, Unicode persistence and external undo/redo. Browser automation cannot select or drive the physical Windows Vietnamese IME, so it must not be used as evidence for this checklist.

## Preconditions

1. Run the application locally and sign in with an approved synthetic Candidate QA account.
2. Open a synthetic CV in `/cv/:id/edit` using current Microsoft Edge or Chrome on Windows.
3. In Windows **Settings → Time & language → Language & region**, install the Vietnamese keyboard that will be tested. Confirm the taskbar input indicator changes to Vietnamese.
4. Keep DevTools closed unless event inspection is required. Never paste account credentials into the console or the report.

## Telex procedure

1. Select Vietnamese Telex and focus the **Họ và tên** contenteditable field.
2. Type the physical key sequences needed to produce `Nguyễn`, `ă`, `ơ`, `ư` and `đ` (including `dd` for `đ`). Confirm intermediate composition text is not saved as a completed edit.
3. While `Nguyễn` is still composing, press Enter. Confirm the IME accepts the composition and the editor does not unexpectedly blur or insert a duplicate newline.
4. Start another composition, click outside the field before it finishes, then complete/cancel it through the IME. Confirm no character is lost and no older server value replaces the composing text.
5. Wait at least two seconds after composition ends. Confirm the saved indicator appears only after the completed text is available.
6. Use Ctrl+Z, Ctrl+Shift+Z and the on-screen undo/redo controls. Confirm caret/text remain consistent.
7. Navigate to Preview, export PDF, return to the editor and reload. Confirm every Vietnamese character remains intact.

## VNI procedure

Repeat the Telex procedure with Vietnamese VNI and physical sequences that produce the same characters, including representative tone keys (`a1`, `a2`) and `d9` for `đ`. Verify composition, caret, blur, Enter, autosave, undo/redo, navigation, export and reload independently from the Telex result.

## Evidence record

Record Windows version, browser version, IME name/version, keyboard mode, tested CV ID, local timestamp and PASS/FAIL for each step. Capture a screenshot after reload and retain it only in the ignored QA evidence directory. Any lost character, premature autosave, caret jump, duplicate newline or corrupted PDF is a release defect and must not be marked PASS.
