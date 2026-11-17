
import time
from pathlib import Path

import pyautogui
import pygetwindow as gw

pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0.5

EVIDENCE = Path("evidencias_busqueda")
EVIDENCE.mkdir(exist_ok=True)

APP_TITLE = "Student Management System"

EXISTING_ROLL = "22ECE310"
NONEXISTING_ROLL = "99ZZZ999"


def activate_app():
    app = gw.getWindowsWithTitle(APP_TITLE)

    if not app:
        raise RuntimeError("No se encontró la aplicación.")

    app[0].activate()
    time.sleep(1)


def search_student(roll):
    pyautogui.hotkey("ctrl", "f")
    time.sleep(0.5)

    pyautogui.hotkey("ctrl", "a")
    pyautogui.write(roll, interval=0.08)

    time.sleep(1.5)


def save_evidence(name):
    pyautogui.screenshot().save(EVIDENCE / name)


try:
    activate_app()

    # SEA-UI-01: Buscar estudiante existente
    print("SEA-UI-01: Buscar estudiante existente")

    search_student(EXISTING_ROLL)
    save_evidence("SEA-UI-01.png")

    print("Búsqueda realizada. Verificar que aparezca Diya Nair.")

    time.sleep(2)

    # SEA-UI-02: Buscar estudiante inexistente
    print("SEA-UI-02: Buscar estudiante inexistente")

    search_student(NONEXISTING_ROLL)
    save_evidence("SEA-UI-02.png")

    print("Búsqueda realizada. Verificar que no existan resultados.")

    time.sleep(2)

    # SEA-UI-03: Limpiar búsqueda
    print("SEA-UI-03: Limpiar búsqueda")

    pyautogui.press("esc")
    time.sleep(2)

    save_evidence("SEA-UI-03.png")

    print("Búsqueda limpiada. Verificar que aparezcan todos los estudiantes.")

    print("\nPruebas finalizadas.")

except Exception as error:
    print("ERROR:", error)
