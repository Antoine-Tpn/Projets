import sys
import time

import rclpy
from rclpy.node import Node
from rclpy.qos import (
    QoSProfile,
    ReliabilityPolicy,
    DurabilityPolicy,
    HistoryPolicy,
)

from std_msgs.msg import String, Int32
from sensor_msgs.msg import Image

from PyQt5 import QtCore
from PyQt5.QtCore import Qt
from PyQt5.QtGui import QImage, QPixmap
from PyQt5.QtWidgets import (
    QApplication,
    QMainWindow,
    QWidget,
    QLabel,
    QPushButton,
    QVBoxLayout,
    QHBoxLayout,
    QGridLayout,
    QSlider,
    QGroupBox,
    QSizePolicy,
)

import pyqtgraph as pg


########################################################################
# PARAMETRES VITESSE
########################################################################

SPEED_MIN = 0
SPEED_MAX = 1000
SPEED_DEFAULT = 500
SPEED_TICK = 100

OBSTACLE_THRESHOLD_CM = 20


########################################################################
# STYLES
########################################################################

STYLE_BOX_GREY = """
QLabel {
    background-color: #eeeeee;
    color: #555555;
    border: 2px solid #777777;
    border-radius: 7px;
    padding: 8px;
    font-size: 15px;
    font-weight: bold;
}
"""

STYLE_BOX_RED = """
QLabel {
    background-color: #ffcccc;
    color: red;
    border: 2px solid red;
    border-radius: 7px;
    padding: 8px;
    font-size: 15px;
    font-weight: bold;
}
"""

STYLE_BOX_GREEN = """
QLabel {
    background-color: #ccffcc;
    color: green;
    border: 2px solid green;
    border-radius: 7px;
    padding: 8px;
    font-size: 15px;
    font-weight: bold;
}
"""

STYLE_SENSOR = """
QLabel {
    background-color: #eeeeee;
    border: 2px solid #555555;
    border-radius: 7px;
    font-size: 17px;
    font-weight: bold;
    padding: 7px;
}
"""

STYLE_DIR_BUTTON = """
QPushButton {
    font-size: 22px;
    font-weight: bold;
    background-color: #eeeeee;
    border: 2px solid #777777;
    border-radius: 7px;
}
QPushButton:hover {
    background-color: #dddddd;
}
QPushButton:pressed {
    background-color: #bbbbbb;
}
"""

STYLE_STOP_BUTTON = """
QPushButton {
    font-size: 13px;
    font-weight: bold;
    color: white;
    background-color: #d32f2f;
    border: 2px solid #a00000;
    border-radius: 7px;
}
QPushButton:hover {
    background-color: #ef5350;
}
QPushButton:pressed {
    background-color: #b71c1c;
}
"""


########################################################################
# THREAD ROS2
########################################################################

class ROS2Thread(QtCore.QThread):

    image_received = QtCore.pyqtSignal(object)
    sensor_received = QtCore.pyqtSignal(object)
    speed_received = QtCore.pyqtSignal(object)

    def __init__(self):
        super().__init__()

        rclpy.init(args=None)

        self.node = Node("py_ihm_node")
        self.running = True

        # ==============================================================
        # PUBLISHERS
        # ==============================================================

        self.publisher_move = self.node.create_publisher(
            String,
            "/command/move",
            10
        )

        self.publisher_speed = self.node.create_publisher(
            Int32,
            "/command/speed",
            10
        )

        self.publisher_tracking_x = self.node.create_publisher(
            Int32,
            "/tracking/x",
            10
        )

        # ==============================================================
        # CAPTEURS
        # ==============================================================

        self.node.create_subscription(
            Int32,
            "/sensor/dist_left",
            lambda m: self.sensor_received.emit(
                ("left", m.data)
            ),
            10
        )

        self.node.create_subscription(
            Int32,
            "/sensor/dist_right",
            lambda m: self.sensor_received.emit(
                ("right", m.data)
            ),
            10
        )

        self.node.create_subscription(
            Int32,
            "/sensor/dist_back",
            lambda m: self.sensor_received.emit(
                ("back", m.data)
            ),
            10
        )

        # ==============================================================
        # VITESSE REELLE
        #
        # Le STM32 publie :
        # /robot/speed
        # type : Int32
        # ==============================================================

        self.node.create_subscription(
            Int32,
            "/robot/speed",
            lambda m: self.speed_received.emit(m.data),
            10
        )

        # ==============================================================
        # CAMERA
        # ==============================================================

        camera_qos = QoSProfile(
            reliability=ReliabilityPolicy.BEST_EFFORT,
            durability=DurabilityPolicy.VOLATILE,
            history=HistoryPolicy.KEEP_LAST,
            depth=1,
        )

        self.node.create_subscription(
            Image,
            "/camera/src_frame",
            lambda m: self.image_received.emit(m),
            camera_qos
        )

        print("\n==========================================")
        print("             IHM ROBOT ROS2")
        print("==========================================")
        print("Commande        : /command/move   (String)")
        print("Vitesse demandee: /command/speed  (Int32)")
        print("Tracking X      : /tracking/x     (Int32)")
        print("Capteurs        : /sensor/dist_left")
        print("                  /sensor/dist_right")
        print("                  /sensor/dist_back")
        print("Vitesse reelle  : /robot/speed   (Int32)")
        print("Camera          : /camera/src_frame")
        print("==========================================\n")

    ####################################################################
    # COMMANDES ROS2
    ####################################################################

    def send_command(self, command):

        msg = String()
        msg.data = command

        self.publisher_move.publish(msg)

        print('Commande envoyee : "%s"' % command)

    def send_speed(self, speed):

        msg = Int32()
        msg.data = int(speed)

        self.publisher_speed.publish(msg)

        print("Vitesse envoyee :", msg.data)

    def send_tracking_x(self, x):

        msg = Int32()
        msg.data = int(x)

        self.publisher_tracking_x.publish(msg)

    ####################################################################
    # THREAD
    ####################################################################

    def run(self):

        while self.running and rclpy.ok():

            rclpy.spin_once(
                self.node,
                timeout_sec=0.01
            )

    def stop(self):

        self.running = False

        if rclpy.ok():

            try:
                self.node.destroy_node()
            except Exception:
                pass

            rclpy.shutdown()

        self.quit()


########################################################################
# FENETRE PRINCIPALE
########################################################################

class MainWindow(QMainWindow):

    def __init__(self, parent=None):

        super().__init__(parent)

        self.setWindowTitle(
            "ROS2 - Commande Robot"
        )

        self.setMinimumSize(
            1200,
            750
        )

        self.resize(
            1400,
            850
        )

        # ==============================================================
        # WIDGET CENTRAL
        # ==============================================================

        central_widget = QWidget()

        self.setCentralWidget(
            central_widget
        )

        main_layout = QVBoxLayout(
            central_widget
        )

        main_layout.setContentsMargins(
            10,
            10,
            10,
            10
        )

        main_layout.setSpacing(
            8
        )

        # ==============================================================
        # TITRE
        # ==============================================================

        title = QLabel(
            "COMMANDE DU ROBOT"
        )

        title.setAlignment(
            Qt.AlignCenter
        )

        title.setStyleSheet(
            "QLabel { "
            "font-size: 26px; "
            "font-weight: bold; "
            "}"
        )

        main_layout.addWidget(
            title
        )

        # ==============================================================
        # MODE
        # ==============================================================

        self.mode_label = QLabel(
            "Mode : Manuel"
        )

        self.mode_label.setAlignment(
            Qt.AlignCenter
        )

        self.mode_label.setStyleSheet(
            "QLabel { "
            "color: #0066cc; "
            "font-size: 18px; "
            "font-weight: bold; "
            "}"
        )

        main_layout.addWidget(
            self.mode_label
        )

        # ==============================================================
        # GRILLE PRINCIPALE
        # ==============================================================

        main_grid = QGridLayout()

        main_grid.setColumnMinimumWidth(
            0,
            650
        )

        main_grid.setColumnMinimumWidth(
            1,
            300
        )

        main_grid.setHorizontalSpacing(
            15
        )

        main_grid.setVerticalSpacing(
            5
        )

        main_layout.addLayout(
            main_grid
        )

        ################################################################
        # COLONNE GAUCHE : CAMERA
        ################################################################

        camera_container = QWidget()

        camera_layout = QVBoxLayout(
            camera_container
        )

        camera_layout.setContentsMargins(
            0,
            0,
            0,
            0
        )

        camera_layout.setSpacing(
            5
        )

        camera_title = QLabel(
            "CAMERA DU ROBOT"
        )

        camera_title.setAlignment(
            Qt.AlignCenter
        )

        camera_title.setStyleSheet(
            "QLabel { "
            "font-size: 18px; "
            "font-weight: bold; "
            "}"
        )

        camera_layout.addWidget(
            camera_title
        )

        self.camera_label = QLabel(
            "AUCUNE IMAGE CAMERA"
        )

        self.camera_label.setMinimumSize(
            640,
            480
        )

        self.camera_label.setSizePolicy(
            QSizePolicy.Expanding,
            QSizePolicy.Expanding
        )

        self.camera_label.setAlignment(
            Qt.AlignCenter
        )

        self.camera_label.setStyleSheet(
            """
            QLabel {
                background-color: black;
                color: white;
                border: 5px solid red;
                font-size: 24px;
                font-weight: bold;
            }
            """
        )

        camera_layout.addWidget(
            self.camera_label,
            1
        )

        self.camera_status = QLabel(
            "CAMERA : DECONNECTEE"
        )

        self.camera_status.setAlignment(
            Qt.AlignCenter
        )

        camera_layout.addWidget(
            self.camera_status
        )

        main_grid.addWidget(
            camera_container,
            0,
            0
        )

        ################################################################
        # COLONNE DROITE
        ################################################################

        right_container = QWidget()

        right_layout = QVBoxLayout(
            right_container
        )

        right_layout.setContentsMargins(
            0,
            0,
            0,
            0
        )

        right_layout.setSpacing(
            8
        )

        # ==============================================================
        # DIRECTIONS
        # ==============================================================

        movement_group = QGroupBox(
            "COMMANDES DE DIRECTION"
        )

        movement_layout = QGridLayout(
            movement_group
        )

        movement_layout.setSpacing(
            4
        )

        self.btn_forward = QPushButton("↑")
        self.btn_left = QPushButton("←")
        self.btn_stop = QPushButton("STOP")
        self.btn_right = QPushButton("→")
        self.btn_backward = QPushButton("↓")

        for button in (
            self.btn_forward,
            self.btn_left,
            self.btn_stop,
            self.btn_right,
            self.btn_backward
        ):

            button.setFixedSize(
                75,
                50
            )

            button.setStyleSheet(
                STYLE_DIR_BUTTON
            )

        self.btn_stop.setStyleSheet(
            STYLE_STOP_BUTTON
        )

        movement_layout.addWidget(
            self.btn_forward,
            0,
            1,
            Qt.AlignCenter
        )

        movement_layout.addWidget(
            self.btn_left,
            1,
            0,
            Qt.AlignCenter
        )

        movement_layout.addWidget(
            self.btn_stop,
            1,
            1,
            Qt.AlignCenter
        )

        movement_layout.addWidget(
            self.btn_right,
            1,
            2,
            Qt.AlignCenter
        )

        movement_layout.addWidget(
            self.btn_backward,
            2,
            1,
            Qt.AlignCenter
        )

        right_layout.addWidget(
            movement_group
        )

        self.btn_forward.clicked.connect(
            lambda: self.send_command("F")
        )

        self.btn_left.clicked.connect(
            lambda: self.send_command("L")
        )

        self.btn_stop.clicked.connect(
            lambda: self.send_command("S")
        )

        self.btn_right.clicked.connect(
            lambda: self.send_command("R")
        )

        self.btn_backward.clicked.connect(
            lambda: self.send_command("B")
        )

        # ==============================================================
        # MODES
        # ==============================================================

        mode_group = QGroupBox(
            "MODE DE FONCTIONNEMENT"
        )

        mode_layout = QHBoxLayout(
            mode_group
        )

        self.btn_manual = QPushButton(
            "MANUEL"
        )

        self.btn_auto = QPushButton(
            "AUTOMATIQUE"
        )

        self.btn_color = QPushButton(
            "TRACKING"
        )

        for button in (
            self.btn_manual,
            self.btn_auto,
            self.btn_color
        ):

            button.setMinimumHeight(
                40
            )

            mode_layout.addWidget(
                button
            )

        right_layout.addWidget(
            mode_group
        )

        self.btn_manual.clicked.connect(
            lambda: self.set_mode(
                "M",
                "Manuel"
            )
        )

        self.btn_auto.clicked.connect(
            lambda: self.set_mode(
                "A",
                "Automatique"
            )
        )

        self.btn_color.clicked.connect(
            lambda: self.set_mode(
                "T",
                "Suivi de couleur"
            )
        )

        # ==============================================================
        # VITESSE
        # ==============================================================

        speed_group = QGroupBox(
            "REGLAGE DE LA VITESSE"
        )

        speed_layout = QVBoxLayout(
            speed_group
        )

        self.speed_label = QLabel(
            f"Vitesse demandee : {SPEED_DEFAULT}"
        )

        self.speed_label.setAlignment(
            Qt.AlignCenter
        )

        self.speed_label.setStyleSheet(
            "QLabel { "
            "font-size: 15px; "
            "font-weight: bold; "
            "}"
        )

        speed_layout.addWidget(
            self.speed_label
        )

        self.speed_slider = QSlider(
            Qt.Horizontal
        )

        self.speed_slider.setRange(
            SPEED_MIN,
            SPEED_MAX
        )

        self.speed_slider.setValue(
            SPEED_DEFAULT
        )

        self.speed_slider.setTickInterval(
            SPEED_TICK
        )

        self.speed_slider.setTickPosition(
            QSlider.TicksBelow
        )

        speed_layout.addWidget(
            self.speed_slider
        )

        self.btn_speed = QPushButton(
            "APPLIQUER"
        )

        self.btn_speed.setFixedHeight(
            35
        )

        speed_layout.addWidget(
            self.btn_speed
        )

        right_layout.addWidget(
            speed_group
        )

        self.speed_slider.valueChanged.connect(
            self.change_speed_label
        )

        self.btn_speed.clicked.connect(
            self.send_speed
        )

        # ==============================================================
        # ETAT ROBOT
        # ==============================================================

        state_group = QGroupBox(
            "ETAT DU ROBOT"
        )

        state_layout = QVBoxLayout(
            state_group
        )

        self.robot_speed_label = QLabel(
            "Vitesse reelle : --"
        )

        self.robot_speed_label.setAlignment(
            Qt.AlignCenter
        )

        self.robot_speed_label.setStyleSheet(
            "QLabel { "
            "font-size: 16px; "
            "font-weight: bold; "
            "}"
        )

        state_layout.addWidget(
            self.robot_speed_label
        )

        self.obstacle_label = QLabel(
            "OBSTACLE : INCONNU"
        )

        self.obstacle_label.setAlignment(
            Qt.AlignCenter
        )

        self.obstacle_label.setStyleSheet(
            STYLE_BOX_GREY
        )

        state_layout.addWidget(
            self.obstacle_label
        )

        right_layout.addWidget(
            state_group
        )

        # ==============================================================
        # CAPTEURS
        # ==============================================================

        sensor_group = QGroupBox(
            "CAPTEURS"
        )

        sensor_layout = QGridLayout(
            sensor_group
        )

        self.distance_left = self._make_sensor(
            sensor_layout,
            "GAUCHE",
            0
        )

        self.distance_right = self._make_sensor(
            sensor_layout,
            "DROITE",
            1
        )

        self.distance_back = self._make_sensor(
            sensor_layout,
            "ARRIERE",
            2
        )

        right_layout.addWidget(
            sensor_group
        )

        # ==============================================================
        # DERNIERE TRAME
        # ==============================================================

        self.sensor_message = QLabel(
            "Derniere trame : --"
        )

        self.sensor_message.setAlignment(
            Qt.AlignCenter
        )

        self.sensor_message.setStyleSheet(
            "QLabel { "
            "color: #777777; "
            "font-size: 12px; "
            "}"
        )

        right_layout.addWidget(
            self.sensor_message
        )

        right_layout.addStretch()

        main_grid.addWidget(
            right_container,
            0,
            1
        )

        main_grid.setColumnStretch(
            0,
            7
        )

        main_grid.setColumnStretch(
            1,
            3
        )

        main_grid.setRowStretch(
            0,
            1
        )

        ################################################################
        # VALEURS CAPTEURS
        ################################################################

        self.sensor_values = {
            "left": None,
            "right": None,
            "back": None
        }

        ################################################################
        # DONNEES COURBE DE VITESSE
        ################################################################

        self.graph_start_time = time.time()

        self.time_data = []
        self.setpoint_data = []
        self.real_speed_data = []

        self.current_setpoint = SPEED_DEFAULT
        self.current_real_speed = 0

        ################################################################
        # COURBE
        ################################################################

        graph_group = QGroupBox(
            "ASSERVISSEMENT DE VITESSE"
        )

        graph_layout = QVBoxLayout(
            graph_group
        )

        self.speed_plot = pg.PlotWidget()

        # ==============================================================
        # APPARENCE
        # ==============================================================

        self.speed_plot.setBackground(
            "w"
        )

        # Axe Y fixe : 0 -> 1000
        self.speed_plot.setYRange(
            0,
            1000,
            padding=0
        )

        # Axe X
        self.speed_plot.setLabel(
            "bottom",
            "Temps",
            units="s"
        )

        self.speed_plot.setLabel(
            "left",
            "Vitesse"
        )

        self.speed_plot.showGrid(
            x=True,
            y=True,
            alpha=0.25
        )

        self.speed_plot.addLegend()

        # ==============================================================
        # STYLE DES AXES
        # ==============================================================

        self.speed_plot.getAxis(
            "left"
        ).setPen(
            pg.mkPen(
                "k",
                width=1
            )
        )

        self.speed_plot.getAxis(
            "bottom"
        ).setPen(
            pg.mkPen(
                "k",
                width=1
            )
        )

        self.speed_plot.getAxis(
            "left"
        ).setTextPen(
            pg.mkPen("k")
        )

        self.speed_plot.getAxis(
            "bottom"
        ).setTextPen(
            pg.mkPen("k")
        )

        # ==============================================================
        # COURBE CONSIGNE
        # ==============================================================

        self.setpoint_curve = self.speed_plot.plot(
            pen=pg.mkPen(
                "r",
                width=3
            ),
            name="Consigne"
        )

        # ==============================================================
        # COURBE VITESSE REELLE
        # ==============================================================

        self.real_speed_curve = self.speed_plot.plot(
            pen=pg.mkPen(
                "b",
                width=3
            ),
            name="Vitesse reelle"
        )

        graph_layout.addWidget(
            self.speed_plot,
            1
        )

        # ==============================================================
        # BOUTON EFFACER
        # ==============================================================

        self.btn_clear_graph = QPushButton(
            "EFFACER LA COURBE"
        )

        self.btn_clear_graph.setFixedHeight(
            35
        )

        self.btn_clear_graph.clicked.connect(
            self.clear_speed_graph
        )

        graph_layout.addWidget(
            self.btn_clear_graph
        )

        graph_group.setMinimumHeight(
            350
        )

        main_layout.addWidget(
            graph_group,
            1
        )

        ################################################################
        # ROS2
        ################################################################

        self.ros_thread = ROS2Thread()

        self.ros_thread.image_received.connect(
            self.image_callback
        )

        self.ros_thread.sensor_received.connect(
            self.sensor_callback
        )

        self.ros_thread.speed_received.connect(
            self.speed_callback
        )

        self.ros_thread.start()

    ####################################################################
    # OUTIL : CREATION D'UN BLOC CAPTEUR
    ####################################################################

    @staticmethod
    def _make_sensor(
        layout,
        title_text,
        column
    ):

        title = QLabel(
            title_text
        )

        title.setAlignment(
            Qt.AlignCenter
        )

        value = QLabel(
            "-- cm"
        )

        value.setAlignment(
            Qt.AlignCenter
        )

        value.setStyleSheet(
            STYLE_SENSOR
        )

        layout.addWidget(
            title,
            0,
            column
        )

        layout.addWidget(
            value,
            1,
            column
        )

        return value

    ####################################################################
    # COMMANDES
    ####################################################################

    def send_command(
        self,
        command
    ):

        self.ros_thread.send_command(
            command
        )

    def set_mode(
        self,
        command,
        mode
    ):

        self.send_command(
            command
        )

        self.mode_label.setText(
            "Mode : " + mode
        )

    ####################################################################
    # VITESSE
    ####################################################################

    def change_speed_label(
        self,
        value
    ):

        self.speed_label.setText(
            f"Vitesse demandee : {value}"
        )

    def send_speed(self):

        # --------------------------------------------------------------
        # NOUVELLE CONSIGNE
        # --------------------------------------------------------------

        speed = self.speed_slider.value()

        self.current_setpoint = speed

        # --------------------------------------------------------------
        # ENVOI AU STM32
        # --------------------------------------------------------------

        self.ros_thread.send_speed(
            speed
        )

        print(
            "Nouvelle consigne :",
            speed
        )

    def speed_callback(
        self,
        speed
    ):

        # --------------------------------------------------------------
        # VITESSE REELLE
        # --------------------------------------------------------------

        self.current_real_speed = speed

        self.robot_speed_label.setText(
            f"Vitesse reelle : {speed}"
        )

        # --------------------------------------------------------------
        # TEMPS
        # --------------------------------------------------------------

        t = (
            time.time()
            - self.graph_start_time
        )

        # --------------------------------------------------------------
        # STOCKAGE
        # --------------------------------------------------------------

        self.time_data.append(
            t
        )

        self.setpoint_data.append(
            self.current_setpoint
        )

        self.real_speed_data.append(
            speed
        )

        # --------------------------------------------------------------
        # COURBE CONSIGNE
        # --------------------------------------------------------------

        self.setpoint_curve.setData(
            self.time_data,
            self.setpoint_data
        )

        # --------------------------------------------------------------
        # COURBE VITESSE REELLE
        # --------------------------------------------------------------

        self.real_speed_curve.setData(
            self.time_data,
            self.real_speed_data
        )

        # --------------------------------------------------------------
        # AXE X
        # --------------------------------------------------------------

        self.speed_plot.setXRange(
            0,
            max(5, t),
            padding=0
        )

        # --------------------------------------------------------------
        # AXE Y
        # --------------------------------------------------------------

        self.speed_plot.setYRange(
            0,
            1000,
            padding=0
        )

    ####################################################################
    # EFFACER COURBE
    ####################################################################

    def clear_speed_graph(self):

        self.graph_start_time = time.time()

        self.time_data.clear()
        self.setpoint_data.clear()
        self.real_speed_data.clear()

        self.setpoint_curve.clear()
        self.real_speed_curve.clear()

        # Retour à la consigne actuelle
        self.current_setpoint = self.speed_slider.value()

        # Réinitialisation de l'axe
        self.speed_plot.setXRange(
            0,
            5,
            padding=0
        )

        self.speed_plot.setYRange(
            0,
            1000,
            padding=0
        )

        print("Courbe effacee.")

    ####################################################################
    # CAPTEURS
    ####################################################################

    def sensor_callback(
        self,
        data
    ):

        sensor, distance = data

        self.sensor_values[sensor] = distance

        names = {
            "left": "GAUCHE",
            "right": "DROITE",
            "back": "ARRIERE"
        }

        labels = {
            "left": self.distance_left,
            "right": self.distance_right,
            "back": self.distance_back
        }

        labels[sensor].setText(
            f"{distance} cm"
        )

        self.sensor_message.setText(
            f"Dernier capteur : "
            f"{names[sensor]} = {distance} cm"
        )

        values = self.sensor_values.values()

        if any(
            v is not None
            and v <= OBSTACLE_THRESHOLD_CM
            for v in values
        ):

            self.obstacle_label.setText(
                "⚠ OBSTACLE DETECTE"
            )

            self.obstacle_label.setStyleSheet(
                STYLE_BOX_RED
            )

        elif all(
            v is None
            for v in values
        ):

            self.obstacle_label.setText(
                "OBSTACLE : INCONNU"
            )

            self.obstacle_label.setStyleSheet(
                STYLE_BOX_GREY
            )

        else:

            self.obstacle_label.setText(
                "✓ AUCUN OBSTACLE"
            )

            self.obstacle_label.setStyleSheet(
                STYLE_BOX_GREEN
            )

    ####################################################################
    # CAMERA
    ####################################################################

    def image_callback(
        self,
        msg
    ):

        try:

            formats = {
                "bgr8": QImage.Format_BGR888,
                "rgb8": QImage.Format_RGB888,
                "mono8": QImage.Format_Grayscale8,
                "bgra8": QImage.Format_ARGB32,
                "rgba8": QImage.Format_RGBA8888,
            }

            fmt = formats.get(
                msg.encoding.lower()
            )

            if fmt is None:

                print(
                    "Encodage non supporte :",
                    msg.encoding
                )

                return

            image = QImage(
                msg.data,
                msg.width,
                msg.height,
                msg.step,
                fmt
            ).copy()

            pixmap = QPixmap.fromImage(
                image
            ).scaled(
                self.camera_label.width(),
                self.camera_label.height(),
                Qt.KeepAspectRatio,
                Qt.SmoothTransformation
            )

            self.camera_label.setPixmap(
                pixmap
            )

            self.camera_status.setText(
                "CAMERA : IMAGE RECUE"
            )

            self.camera_status.setStyleSheet(
                "QLabel { "
                "color: green; "
                "font-size: 13px; "
                "font-weight: bold; "
                "}"
            )

        except Exception as e:

            print(
                "Erreur camera :",
                e
            )

            self.camera_status.setText(
                "CAMERA : ERREUR"
            )

    ####################################################################
    # FERMETURE
    ####################################################################

    def closeEvent(
        self,
        event
    ):

        print(
            "Fermeture de l'IHM..."
        )

        if self.ros_thread.isRunning():

            self.ros_thread.stop()

            self.ros_thread.wait()

        event.accept()


########################################################################
# MAIN
########################################################################

def main(
    args=None
):

    app = QApplication(
        sys.argv
    )

    window = MainWindow()

    window.show()

    sys.exit(
        app.exec_()
    )


if __name__ == "__main__":

    main()

