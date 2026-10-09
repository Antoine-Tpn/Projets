#include "mainwindow.h"
#include <QWidget>
#include <QHBoxLayout>
#include <QVBoxLayout>
#include <QGroupBox>
#include <QDebug>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
{
    // 1. Instanciation du port série
    serial = new QSerialPort(this);

    // 2. Création de la zone graphique de connexion
    QWidget *centralWidget = new QWidget(this);
    QVBoxLayout *layoutPrincipal = new QVBoxLayout(centralWidget);

    QGroupBox *groupeConnexion = new QGroupBox("Liaison Série STM32", this);
    QHBoxLayout *layoutConnexion = new QHBoxLayout(groupeConnexion);

    comboPorts = new QComboBox(this);
    btnRefresh = new QPushButton("Rafraîchir", this);
    btnConnect = new QPushButton("Connecter", this);
    btn_start = new QPushButton("Start", this);
    btn_stop = new QPushButton("Stop", this);

    lbl_vitesse = new QLabel("Vitesse en tr/min", this);
    lbl_courant = new QLabel("Courant en Ampère", this);

    spinbox_moteur = new QSpinBox(this);
    spinbox_moteur->setRange(0, 1000);
    chkBox_sens = new QCheckBox(this);

    lcd_vitesse = new QLCDNumber(this);
    lcd_courant = new QLCDNumber(this);

    layoutConnexion->addWidget(comboPorts);
    layoutConnexion->addWidget(btnRefresh);
    layoutConnexion->addWidget(btnConnect);

    ledStatut = new QLabel(this);
    ledStatut->setFixedSize(14, 14);
    lblStatutTexte = new QLabel("Déconnecté", this);
    mettreAJourStatut(false);

    layoutConnexion->addSpacing(10);
    layoutConnexion->addWidget(ledStatut);
    layoutConnexion->addWidget(lblStatutTexte);

    layoutPrincipal->addWidget(groupeConnexion);

    QGroupBox *groupeCommande = new QGroupBox("Commande Moteur", this);
    QVBoxLayout *layoutCommande = new QVBoxLayout(groupeCommande);

    QHBoxLayout *layoutConsigne = new QHBoxLayout();
    layoutConsigne->addWidget(new QLabel("Consigne (tr/min) :", this));
    layoutConsigne->addWidget(spinbox_moteur);
    layoutConsigne->addWidget(chkBox_sens);
    chkBox_sens->setText("Sens inverse");

    QHBoxLayout *layoutBoutons = new QHBoxLayout();
    layoutBoutons->addWidget(btn_start);
    layoutBoutons->addWidget(btn_stop);

    btn_urgence = new QPushButton("ARRÊT D'URGENCE", this);
    btn_urgence->setStyleSheet(
        "QPushButton {"
        "   background-color: #d32f2f;"
        "   color: white;"
        "   font-weight: bold;"
        "   padding: 6px 12px;"
        "   border-radius: 4px;"
        "}"
        "QPushButton:hover { background-color: #b71c1c; }"
    );
    layoutBoutons->addWidget(btn_urgence);

    layoutCommande->addLayout(layoutConsigne);
    layoutCommande->addLayout(layoutBoutons);
    layoutPrincipal->addWidget(groupeCommande);

    QGroupBox *groupeMesures = new QGroupBox("Mesures Capteurs", this);
    QHBoxLayout *layoutMesures = new QHBoxLayout(groupeMesures);

    QVBoxLayout *colVitesse = new QVBoxLayout();
    colVitesse->addWidget(lbl_vitesse);
    colVitesse->addWidget(lcd_vitesse);

    QVBoxLayout *colCourant = new QVBoxLayout();
    colCourant->addWidget(lbl_courant);
    colCourant->addWidget(lcd_courant);

    layoutMesures->addLayout(colVitesse);
    layoutMesures->addLayout(colCourant);
    layoutPrincipal->addWidget(groupeMesures);

    layoutPrincipal->addStretch();

    setCentralWidget(centralWidget);

    connect(btnRefresh, &QPushButton::clicked, this, &MainWindow::actualiserPorts);
    connect(btnConnect, &QPushButton::clicked, this, &MainWindow::gererConnexion);
    connect(btn_start,  &QPushButton::clicked, this, &MainWindow::demarrerMoteur);
    connect(btn_stop,   &QPushButton::clicked, this, &MainWindow::arreterMoteur);
    connect(serial,     &QSerialPort::readyRead, this, &MainWindow::lireDonneesSerie);
    connect(btn_urgence, &QPushButton::clicked, this, &MainWindow::arretUrgence);

    // Scan initial des ports disponibles au lancement
    actualiserPorts();
}

MainWindow::~MainWindow()
{
    if (serial->isOpen()) {
        serial->close();
    }
}

void MainWindow::actualiserPorts()
{
    comboPorts->clear();
    // Liste tous les périphériques COM connectés au PC
    const auto infos = QSerialPortInfo::availablePorts();
    for (const QSerialPortInfo &info : infos) {
        comboPorts->addItem(info.portName());
    }
}

void MainWindow::gererConnexion()
{
    if (!serial->isOpen()) {
        QString portChoisi = comboPorts->currentText();
        if (portChoisi.isEmpty()) {
            return;
        }

        // Paramétrage standard de la liaison UART
        serial->setPortName(portChoisi);
        serial->setBaudRate(QSerialPort::Baud115200);
        serial->setDataBits(QSerialPort::Data8);
        serial->setParity(QSerialPort::NoParity);
        serial->setStopBits(QSerialPort::OneStop);
        serial->setFlowControl(QSerialPort::NoFlowControl);

        if (serial->open(QIODevice::ReadWrite)) {
            btnConnect->setText("Déconnecter");
            mettreAJourStatut(true);
            comboPorts->setEnabled(false);
            qDebug() << "Port ouvert :" << portChoisi;
        }
    } else {
        serial->close();
        btnConnect->setText("Connecter");
        mettreAJourStatut(false);
        comboPorts->setEnabled(true);
        qDebug() << "Port fermé.";
    }
}

void MainWindow::demarrerMoteur()
{
    int consigne = spinbox_moteur->value();
    int sens = chkBox_sens->isChecked() ? 1 : 0;

    qDebug() << "Ordre START envoyé : Consigne =" << consigne << ", Sens =" << sens;

    if (serial->isOpen()) {
        // Format de trame envoyé : CMD:consigne,sens\n
        QString trame = QString("CMD:%1,%2\n").arg(consigne).arg(sens);
        serial->write(trame.toUtf8());
    }
}

// Arrêt du moteur
void MainWindow::arreterMoteur()
{
    qDebug() << "Ordre STOP envoyé";

    if (serial->isOpen()) {
        // Format de trame d'arrêt : consigne 0
        serial->write("CMD:0,0\n");
    }
}
void MainWindow::mettreAJourStatut(bool connecte)
{
    if (connecte) {
        ledStatut->setStyleSheet("background-color: #2ecc71; border-radius: 7px;");
        lblStatutTexte->setText("Connecté");
        lblStatutTexte->setStyleSheet("color: #27ae60; font-weight: bold;");
    } else {
        ledStatut->setStyleSheet("background-color: #e74c3c; border-radius: 7px;");
        lblStatutTexte->setText("Déconnecté");
        lblStatutTexte->setStyleSheet("color: #c0392b; font-weight: bold;");
    }
}

void MainWindow::arretUrgence()
{
    spinbox_moteur->setValue(0);
    if (serial->isOpen()) {
        serial->write("CMD:0,0\n");
    }
}

// Réception des mesures de la STM32, gestion du tampon et mise à jour des LCD
void MainWindow::lireDonneesSerie()
{
    bufferSerie.append(serial->readAll());

    while (bufferSerie.contains('\n')) {
        int idx = bufferSerie.indexOf('\n');
        QByteArray ligne = bufferSerie.left(idx).trimmed();
        bufferSerie.remove(0, idx + 1);

        QStringList valeurs = QString::fromUtf8(ligne).split(',');
        if (valeurs.size() >= 2) {
            bool okV = false;
            bool okI = false;
            double v = valeurs[0].toDouble(&okV);
            double i = valeurs[1].toDouble(&okI);

            if (okV && okI) {
                lcd_vitesse->display(v);
                lcd_courant->display(i);
            }
        }
    }
}
