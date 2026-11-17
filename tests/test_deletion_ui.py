import time
import sqlite3
from pathlib import Path

import pyautogui
import pygetwindow as gw

pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0.5

DB_PATH = Path("data/students.db")
EVIDENCE = Path("evidencias_eliminacion")
EVIDENCE.mkdir(exist_ok=True)

REF_WIDTH = 1223
REF_HEIGHT = 768

TARGET_ROLL = "23CSE118"


def get_window(title):
    windows = gw.getWindowsWithTitle(title)
    return windows[0] if windows else None


def wait_window(title, timeout=6):
    end = time.time() + timeout
    while time.time() < end:
        window = get_window(title)
        if window:
            return window
        time.sleep(0.2)
    return None


def activate_app():
    shortcuts = get_window("Keyboard shortcuts")
    if shortcuts:
        shortcuts.activate()
        time.sleep(0.4)
        pyautogui.press("enter")
        time.sleep(0.8)

    app = get_window("Student Management System")
    if not app:
        raise RuntimeError("No se encontró la ventana Student Management System.")

    app.activate()
    app.maximize()
    time.sleep(1.5)
    return app

def save_evidence(name):
    pyautogui.screenshot().save(EVIDENCE / name)
    print(f"Captura: {name}")


def read_database():
    if not DB_PATH.exists():
        raise FileNotFoundError(f"No existe la base de datos: {DB_PATH}")

    with sqlite3.connect(str(DB_PATH), timeout=5) as connection:
        total = connection.execute(
            "SELECT COUNT(*) FROM student"
        ).fetchone()[0]

        student = connection.execute(
            "SELECT id, name FROM student WHERE roll_no = ?",
            (TARGET_ROLL,)
        ).fetchone()

    return total, student

def click_app(app, x, y):
    screen_x = app.left + round(x * app.width / REF_WIDTH)
    screen_y = app.top + round(y * app.height / REF_HEIGHT)

    pyautogui.moveTo(screen_x, screen_y, duration=0.3)
    pyautogui.click()

    print(f"Clic en la aplicación: ({screen_x}, {screen_y})")


def show_result(test_id, passed, detail):
    print(f"{test_id}: {'PASSED' if passed else 'FAILED'} - {detail}")


try:
    print("Iniciando pruebas...")
    app = activate_app()

    # DEL-02: sin estudiante seleccionado ni cargado.
    print("\nDEL-02: eliminar sin selección")
    before_total, _ = read_database()

    pyautogui.press("esc")
    time.sleep(1)

    pyautogui.hotkey("delete")
    time.sleep(1)

    after_total, _ = read_database()
    dialog = get_window("Confirm delete")

    del02_passed = after_total == before_total and dialog is None
    show_result(
        "DEL-02",
        del02_passed,
        f"registros antes={before_total}, después={after_total}; "
        f"sin confirmación de borrado={dialog is None}"
    )

    if dialog:
        dialog.activate()
        pyautogui.press("esc")
        time.sleep(0.7)

    # DEL-03: buscar un estudiante, cargarlo y hacer clic en el botón Delete.
    print("\nDEL-03: cancelar eliminación")

    app.activate()
    # DEL-03: secuencia de eliminación

    # 1. Presionar ESC
    pyautogui.press("esc")
    time.sleep(1)

    # 2. Hacer doble clic en el centro inferior de la pantalla
    screen_width, screen_height = pyautogui.size()

    x = screen_width // 2
    y = int(screen_height * 0.75)

    pyautogui.doubleClick(x, y, interval=0.2)
    time.sleep(1)

    # 3. Presionar flecha abajo
    pyautogui.press("down")
    time.sleep(0.5)

    # 4. Presionar DELETE
    pyautogui.press("delete")
    time.sleep(2)

    # 5. Presionar flecha derecha
    pyautogui.press("tab")
    time.sleep(0.5)

    # 6. Presionar ENTER
    pyautogui.press("enter")
    time.sleep(2)

    # Guardar evidencia
    save_evidence("DEL-03_resultado.png")

except Exception as error:
    confirm = get_window("Confirm delete")
    if confirm:
        confirm.activate()
        pyautogui.press("esc")
    print(f"\nERROR: {error}")
