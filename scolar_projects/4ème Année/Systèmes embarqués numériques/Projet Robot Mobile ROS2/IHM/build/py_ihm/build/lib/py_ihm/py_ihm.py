

import sys

import rclpy

from rclpy.node import Node

from rclpy.qos import (
    QoSProfile,
    ReliabilityPolicy,
    DurabilityPolicy,
    HistoryPolicy
)

from std_msgs.msg import String
from std_msgs.msg import Float32
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
    QSizePolicy
)


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

        self.node = Node(
            'py_ihm_node'
        )

        self.running = True

        # ==============================================================
        # PUBLISHER COMMANDES
        # ==============================================================

        self.publisher_move = self.node.create_publisher(
            String,
            '/command/move',
            10
        )

        # ==============================================================
        # PUBLISHER VITESSE
        # ==============================================================

        self.publisher_speed = self.node.create_publisher(
            Float32,
            '/command/speed',
            10
        )

        # ==============================================================
        # SUBSCRIBER CAPTEURS
        # ==============================================================

        self.subscription_sensor = self.node.create_subscription(
            String,
            '/sensor',
            self.sensor_callback,
            10
        )

        # ==============================================================
        # SUBSCRIBER VITESSE ROBOT
        # ==============================================================

        self.subscription_speed = self.node.create_subscription(
            Float32,
            '/robot/speed',
            self.speed_callback,
            10
        )

        # ==============================================================
        # SUBSCRIBER CAMERA
        # ==============================================================

        camera_qos = QoSProfile(
            reliability=ReliabilityPolicy.BEST_EFFORT,
            durability=DurabilityPolicy.VOLATILE,
            history=HistoryPolicy.KEEP_LAST,
            depth=1
        )

        self.subscription_camera = self.node.create_subscription(
            Image,
            '/camera/src_frame',
            self.image_callback,
            camera_qos
        )

        print("")
        print("==========================================")
        print("             IHM ROBOT ROS2")
        print("==========================================")
        print("Commande : /command/move")
        print("Vitesse  : /command/speed")
        print("Capteurs : /sensor")
        print("Vitesse  : /robot/speed")
        print("Camera   : /camera/src_frame")
        print("==========================================")
        print("")

    ####################################################################
    # CALLBACK SENSOR
    ####################################################################

    def sensor_callback(
        self,
        msg
    ):

        self.sensor_received.emit(
            msg
        )

    ####################################################################
    # CALLBACK VITESSE
    ####################################################################

    def speed_callback(
        self,
        msg
    ):

        self.speed_received.emit(
            msg
        )

    ####################################################################
    # CALLBACK CAMERA
    ####################################################################

    def image_callback(
        self,
        msg
    ):

        self.image_received.emit(
            msg
        )

    ####################################################################
    # ENVOI COMMANDE
    ####################################################################

    def send_command(
        self,
        command
    ):

        msg = String()

        msg.data = command

        self.publisher_move.publish(
            msg
        )

        print(
            'Commande envoyée : "%s"' % command
        )

    ####################################################################
    # ENVOI VITESSE
    ####################################################################

    def send_speed(
        self,
        speed
    ):

        msg = Float32()

        msg.data = float(
            speed
        )

        self.publisher_speed.publish(
            msg
        )

        print(
            "Vitesse envoyée :",
            speed
        )

    ####################################################################
    # THREAD
    ####################################################################

    def run(self):

        while (
            self.running
            and
            rclpy.ok()
        ):

            rclpy.spin_once(
                self.node,
                timeout_sec=0.01
            )

    ####################################################################
    # ARRET
    ####################################################################

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

    def __init__(
        self,
        parent=None
    ):

        super().__init__(
            parent
        )

        # ==============================================================
        # FENETRE
        # ==============================================================

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

        # ==============================================================
        # LAYOUT GENERAL
        # ==============================================================

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
            """
            QLabel {
                font-size: 26px;
                font-weight: bold;
            }
            """
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
            """
            QLabel {
                color: #0066cc;
                font-size: 18px;
                font-weight: bold;
            }
            """
        )

        main_layout.addWidget(
            self.mode_label
        )

        # ==============================================================
        # GRAND GRID PRINCIPAL
        # ==============================================================

        main_grid = QGridLayout()
        main_grid.setColumnMinimumWidth(0, 650)
        main_grid.setColumnMinimumWidth(1, 300)


        main_grid.setHorizontalSpacing(
            15
        )

        main_grid.setVerticalSpacing(
            5
        )

        main_layout.addLayout(
            main_grid
        )


        # ==============================================================
        # COLONNE GAUCHE : CAMERA
        # ==============================================================

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




        # ==============================================================
        # TITRE CAMERA
        # ==============================================================

        camera_title = QLabel(
            "CAMERA DU ROBOT"
        )

        camera_title.setAlignment(
            Qt.AlignCenter
        )

        camera_title.setStyleSheet(
            """
            QLabel {
                font-size: 18px;
                font-weight: bold;
            }
            """
        )

        camera_layout.addWidget(
            camera_title
        )

        # ==============================================================
        # ECRAN CAMERA
        # ==============================================================

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

        self.camera_label.setStyleSheet("""
            QLabel {
                background-color: black;
                color: white;
                border: 5px solid red;
                font-size: 24px;
                font-weight: bold;
            }
        """)

        camera_layout.addWidget(
            self.camera_label,
            1
        )


        # ==============================================================
        # IMAGE NOIRE INITIALE
        #
        # CETTE IMAGE EST CREEE AVANT MEME QUE LA CAMERA ENVOIE
        # QUOI QUE CE SOIT.
        # ==============================================================

        

        # ==============================================================
        # ETAT CAMERA
        # ==============================================================

        self.camera_status = QLabel(
            "CAMERA : DECONNECTEE"
        )

        self.camera_status.setAlignment(
            Qt.AlignCenter
        )

        camera_layout.addWidget(
            self.camera_status
        )


        # ==============================================================
        # AJOUT COLONNE GAUCHE
        # ==============================================================

        main_grid.addWidget(
            camera_container,
            0,
            0
        )

        ################################################################
        # ==============================================================
        # COLONNE DROITE
        # ==============================================================
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

        ################################################################
        # ==============================================================
        # COMMANDES DE DIRECTION
        # ==============================================================
        ################################################################

        movement_group = QGroupBox(
            "COMMANDES DE DIRECTION"
        )

        movement_layout = QGridLayout(
            movement_group
        )

        movement_layout.setSpacing(
            4
        )

        # ==============================================================
        # BOUTONS
        # ==============================================================

        self.btn_forward = QPushButton(
            "↑"
        )

        self.btn_left = QPushButton(
            "←"
        )

        self.btn_stop = QPushButton(
            "STOP"
        )

        self.btn_right = QPushButton(
            "→"
        )

        self.btn_backward = QPushButton(
            "↓"
        )

        direction_buttons = [
            self.btn_forward,
            self.btn_left,
            self.btn_stop,
            self.btn_right,
            self.btn_backward
        ]

        for button in direction_buttons:

            button.setFixedSize(
                75,
                50
            )

            button.setStyleSheet(
                """
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
            )

        # ==============================================================
        # STOP
        # ==============================================================

        self.btn_stop.setStyleSheet(
            """
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
        )

        # ==============================================================
        # PLACEMENT
        # ==============================================================

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

        ################################################################
        # COMMANDES
        ################################################################

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

        ################################################################
        # ==============================================================
        # MODES
        # ==============================================================
        ################################################################

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
            "SUIVI COULEUR"
        )

        for button in [
            self.btn_manual,
            self.btn_auto,
            self.btn_color
        ]:

            button.setMinimumHeight(
                40
            )

        mode_layout.addWidget(
            self.btn_manual
        )

        mode_layout.addWidget(
            self.btn_auto
        )

        mode_layout.addWidget(
            self.btn_color
        )

        right_layout.addWidget(
            mode_group
        )

        ################################################################
        # ACTIONS MODES
        ################################################################

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
                "C",
                "Suivi de couleur"
            )
        )

        ################################################################
        # ==============================================================
        # VITESSE
        # ==============================================================
        ################################################################

        speed_group = QGroupBox(
            "REGLAGE DE LA VITESSE"
        )

        speed_layout = QVBoxLayout(
            speed_group
        )

        # ==============================================================
        # LABEL VITESSE
        # ==============================================================

        self.speed_label = QLabel(
            "Vitesse demandée : 50 %"
        )

        self.speed_label.setAlignment(
            Qt.AlignCenter
        )

        self.speed_label.setStyleSheet(
            """
            QLabel {
                font-size: 15px;
                font-weight: bold;
            }
            """
        )

        speed_layout.addWidget(
            self.speed_label
        )

        # ==============================================================
        # SLIDER
        # ==============================================================

        self.speed_slider = QSlider(
            Qt.Horizontal
        )

        self.speed_slider.setRange(
            0,
            100
        )

        self.speed_slider.setValue(
            50
        )

        self.speed_slider.setTickInterval(
            10
        )

        self.speed_slider.setTickPosition(
            QSlider.TicksBelow
        )

        speed_layout.addWidget(
            self.speed_slider
        )

        # ==============================================================
        # BOUTON VITESSE
        # ==============================================================

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

        ################################################################
        # ==============================================================
        # ETAT ROBOT
        # ==============================================================
        ################################################################

        state_group = QGroupBox(
            "ETAT DU ROBOT"
        )

        state_layout = QVBoxLayout(
            state_group
        )

        # ==============================================================
        # VITESSE REELLE
        # ==============================================================

        self.robot_speed_label = QLabel(
            "Vitesse réelle : -- m/s"
        )

        self.robot_speed_label.setAlignment(
            Qt.AlignCenter
        )

        self.robot_speed_label.setStyleSheet(
            """
            QLabel {
                font-size: 16px;
                font-weight: bold;
            }
            """
        )

        state_layout.addWidget(
            self.robot_speed_label
        )

        # ==============================================================
        # OBSTACLE
        # ==============================================================

        self.obstacle_label = QLabel(
            "OBSTACLE : INCONNU"
        )

        self.obstacle_label.setAlignment(
            Qt.AlignCenter
        )

        self.obstacle_label.setStyleSheet(
            """
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
        )

        state_layout.addWidget(
            self.obstacle_label
        )

        right_layout.addWidget(
            state_group
        )

        ################################################################
        # ==============================================================
        # CAPTEURS
        # ==============================================================
        ################################################################

        sensor_group = QGroupBox(
            "CAPTEURS"
        )

        sensor_layout = QGridLayout(
            sensor_group
        )

        # ==============================================================
        # GAUCHE
        # ==============================================================

        left_title = QLabel(
            "GAUCHE"
        )

        left_title.setAlignment(
            Qt.AlignCenter
        )

        self.distance_left = QLabel(
            "-- cm"
        )

        self.distance_left.setAlignment(
            Qt.AlignCenter
        )

        self.distance_left.setStyleSheet(
            """
            QLabel {
                background-color: #eeeeee;
                border: 2px solid #555555;
                border-radius: 7px;
                font-size: 17px;
                font-weight: bold;
                padding: 7px;
            }
            """
        )

        sensor_layout.addWidget(
            left_title,
            0,
            0
        )

        sensor_layout.addWidget(
            self.distance_left,
            1,
            0
        )

        # ==============================================================
        # DROITE
        # ==============================================================

        right_title = QLabel(
            "DROITE"
        )

        right_title.setAlignment(
            Qt.AlignCenter
        )

        self.distance_right = QLabel(
            "-- cm"
        )

        self.distance_right.setAlignment(
            Qt.AlignCenter
        )

        self.distance_right.setStyleSheet(
            """
            QLabel {
                background-color: #eeeeee;
                border: 2px solid #555555;
                border-radius: 7px;
                font-size: 17px;
                font-weight: bold;
                padding: 7px;
            }
            """
        )

        sensor_layout.addWidget(
            right_title,
            0,
            1
        )

        sensor_layout.addWidget(
            self.distance_right,
            1,
            1
        )

        # ==============================================================
        # ARRIERE
        # ==============================================================

        rear_title = QLabel(
            "ARRIERE"
        )

        rear_title.setAlignment(
            Qt.AlignCenter
        )

        self.distance_rear = QLabel(
            "-- cm"
        )

        self.distance_rear.setAlignment(
            Qt.AlignCenter
        )

        self.distance_rear.setStyleSheet(
            """
            QLabel {
                background-color: #eeeeee;
                border: 2px solid #555555;
                border-radius: 7px;
                font-size: 17px;
                font-weight: bold;
                padding: 7px;
            }
            """
        )

        sensor_layout.addWidget(
            rear_title,
            0,
            2
        )

        sensor_layout.addWidget(
            self.distance_rear,
            1,
            2
        )

        right_layout.addWidget(
            sensor_group
        )

        ################################################################
        # DERNIERE TRAME
        ################################################################

        self.sensor_message = QLabel(
            "Dernière trame : --"
        )

        self.sensor_message.setAlignment(
            Qt.AlignCenter
        )

        self.sensor_message.setStyleSheet(
            """
            QLabel {
                color: #777777;
                font-size: 12px;
            }
            """
        )

        right_layout.addWidget(
            self.sensor_message
        )

        ################################################################
        # ESPACE LIBRE
        ################################################################

        right_layout.addStretch()

        ################################################################
        # AJOUT COLONNE DROITE
        ################################################################

        main_grid.addWidget(
            right_container,
            0,
            1
        )

        ################################################################
        # PROPORTIONS DU GRID
        ################################################################

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
        # ==============================================================
        # ROS2
        # ==============================================================
        ################################################################

        self.ros_thread = ROS2Thread()

        # ==============================================================
        # SIGNAL CAMERA
        # ==============================================================

        self.ros_thread.image_received.connect(
            self.image_callback
        )

        # ==============================================================
        # SIGNAL CAPTEURS
        # ==============================================================

        self.ros_thread.sensor_received.connect(
            self.sensor_callback
        )

        # ==============================================================
        # SIGNAL VITESSE
        # ==============================================================

        self.ros_thread.speed_received.connect(
            self.speed_callback
        )

        # ==============================================================
        # DEMARRAGE
        # ==============================================================

        self.ros_thread.start()

    ####################################################################
    # ENVOI COMMANDE
    ####################################################################

    def send_command(
        self,
        command
    ):

        self.ros_thread.send_command(
            command
        )

    ####################################################################
    # MODE
    ####################################################################

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
    # LABEL VITESSE
    ####################################################################

    def change_speed_label(
        self,
        value
    ):

        self.speed_label.setText(
            f"Vitesse demandée : {value} %"
        )

    ####################################################################
    # ENVOI VITESSE
    ####################################################################

    def send_speed(
        self
    ):

        value = self.speed_slider.value()

        speed = value / 100.0

        self.ros_thread.send_speed(
            speed
        )

    ####################################################################
    # RECEPTION VITESSE
    ####################################################################

    def speed_callback(
        self,
        msg
    ):

        self.robot_speed_label.setText(
            f"Vitesse réelle : {msg.data:.2f} m/s"
        )

    ####################################################################
    # RECEPTION CAPTEURS
    ####################################################################

    def sensor_callback(
        self,
        msg
    ):

        print(
            'Sensor : "%s"' % msg.data
        )

        self.sensor_message.setText(
            "Dernière trame : " + msg.data
        )

        values = msg.data.split()

        if len(values) != 3:

            print(
                "Erreur : trame sensor incorrecte"
            )

            return

        try:

            left = float(
                values[0]
            )

            right = float(
                values[1]
            )

            rear = float(
                values[2]
            )

        except ValueError:

            print(
                "Erreur : distances invalides"
            )

            return

        # ==============================================================
        # AFFICHAGE DISTANCES
        # ==============================================================

        self.distance_left.setText(
            f"{left:.1f} cm"
        )

        self.distance_right.setText(
            f"{right:.1f} cm"
        )

        self.distance_rear.setText(
            f"{rear:.1f} cm"
        )

        # ==============================================================
        # DETECTION OBSTACLE
        # ==============================================================

        threshold = 20.0

        obstacle = (
            left <= threshold
            or
            right <= threshold
            or
            rear <= threshold
        )

        if obstacle:

            self.obstacle_label.setText(
                "⚠ OBSTACLE DETECTE"
            )

            self.obstacle_label.setStyleSheet(
                """
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
            )

        else:

            self.obstacle_label.setText(
                "✓ AUCUN OBSTACLE"
            )

            self.obstacle_label.setStyleSheet(
                """
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
            )

    ####################################################################
    # RECEPTION IMAGE CAMERA
    ####################################################################

    def image_callback(
        self,
        msg
    ):

        try:

            print(
                "IMAGE RECUE :",
                msg.width,
                "x",
                msg.height,
                "encoding =",
                msg.encoding
            )

            # ==========================================================
            # BGR8
            # ==========================================================

            if msg.encoding.lower() == "bgr8":

                image = QImage(
                    msg.data,
                    msg.width,
                    msg.height,
                    msg.step,
                    QImage.Format_BGR888
                )

            # ==========================================================
            # RGB8
            # ==========================================================

            elif msg.encoding.lower() == "rgb8":

                image = QImage(
                    msg.data,
                    msg.width,
                    msg.height,
                    msg.step,
                    QImage.Format_RGB888
                )

            # ==========================================================
            # MONO8
            # ==========================================================

            elif msg.encoding.lower() == "mono8":

                image = QImage(
                    msg.data,
                    msg.width,
                    msg.height,
                    msg.step,
                    QImage.Format_Grayscale8
                )

            # ==========================================================
            # BGRA8
            # ==========================================================

            elif msg.encoding.lower() == "bgra8":

                image = QImage(
                    msg.data,
                    msg.width,
                    msg.height,
                    msg.step,
                    QImage.Format_ARGB32
                )

            # ==========================================================
            # RGBA8
            # ==========================================================

            elif msg.encoding.lower() == "rgba8":

                image = QImage(
                    msg.data,
                    msg.width,
                    msg.height,
                    msg.step,
                    QImage.Format_RGBA8888
                )

            else:

                print(
                    "Encodage non supporté :",
                    msg.encoding
                )

                return

            # ==========================================================
            # COPIE DE SECURITE
            # ==========================================================

            image = image.copy()

            # ==========================================================
            # QIMAGE -> QPIXMAP
            # ==========================================================

            pixmap = QPixmap.fromImage(
                image
            )

            # ==========================================================
            # TAILLE DISPONIBLE
            # ==========================================================

            width = self.camera_label.width()
            height = self.camera_label.height()

            # ==========================================================
            # REDIMENSIONNEMENT
            # ==========================================================

            pixmap = pixmap.scaled(
                width,
                height,
                Qt.KeepAspectRatio,
                Qt.SmoothTransformation
            )

            # ==========================================================
            # AFFICHAGE
            # ==========================================================

            self.camera_label.setPixmap(
                pixmap
            )

            # ==========================================================
            # ETAT CAMERA
            # ==========================================================

            self.camera_status.setText(
                "CAMERA : IMAGE RECUE"
            )

            self.camera_status.setStyleSheet(
                """
                QLabel {
                    color: green;
                    font-size: 13px;
                    font-weight: bold;
                }
                """
            )

        except Exception as e:

            print(
                "Erreur caméra :",
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


########################################################################
# PROGRAMME
########################################################################

if __name__ == "__main__":

    main()
