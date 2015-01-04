#include "SparkButton.h"

#define SET_ALARM_PORT 42620
#define DISCOVERY_PORT 42621

UDP Discovery;
UDP SetAlarm;

#define ONE_DAY_MILLIS (24 * 60 * 60 * 1000)
unsigned long lastSync = millis();

#define RED 0
#define GREEN 1
#define BLUE 2
#define HOUR 3
#define MINUTE 4
#define SET 5

byte alarm[6];

SparkButton btn = SparkButton();

void setup() {
	Discovery.begin(DISCOVERY_PORT);
	SetAlarm.begin(SET_ALARM_PORT);
	btn.begin();
	for (byte b = 0; b < 6; b++) alarm[b] = EEPROM.read(b);
}

void loop() {
	byte buf[64];
	if (Discovery.parsePacket() > 0) {
		Discovery.flush();
		String myID = Spark.deviceID();
		myID.toCharArray((char*)buf, 64);
		Discovery.beginPacket(Discovery.remoteIP(), DISCOVERY_PORT);
		Discovery.write(buf, myID.length());
		Discovery.endPacket();
	}
	if (SetAlarm.parsePacket() > 4) {
		SetAlarm.read((char*)alarm, 5);
		SetAlarm.flush();
		SetAlarm.beginPacket(SetAlarm.remoteIP(), SET_ALARM_PORT);
		SetAlarm.write((byte*)"ACK", 3);
		SetAlarm.endPacket();
		alarm[SET] = 1;
		for (byte b = 0; b < 6; b++) EEPROM.write(b, alarm[b]);
	}
	if (alarm[SET] && alarm[HOUR] == Time.hour() && alarm[MINUTE] == Time.minute()) {
		for (byte b = 0; b <= 10; b++) {
			for (byte c = 1; c < 12; c++) btn.ledOn(c, alarm[RED] * b / 10,
				alarm[GREEN] * b / 10, alarm[BLUE] * b / 10);
			delay(300);
		}
		playMelody();
	}
	if (millis() - lastSync > ONE_DAY_MILLIS) {
		// Request time synchronization from the Spark Cloud
		Spark.syncTime();
		lastSync = millis();
	}
}

#define speakerPin A0

// notes in the melody:
// http://imslp.org/wiki/Peer_Gynt_Suite_No.1,_Op.46_%28Grieg,_Edvard%29#Full_Scores

// H, #G, #F // E, #F, #G // H, [#G, A], #G // #F, E, [#F, #G, #F, #G]
// [#G, A], H, #G, H // #C, #G, #C // H, #G, #F, (E)

// http://www.phy.mtu.edu/~suits/notefreqs.html

int melody[] = {
	1976, 1661, 1480,
	1319, 1480, 1661,
	1976, 1661, 1760, 1661, 1480,
	1319, 1480, 1661, 1480, 1661,

	1661, 1760, 1976, 1661, 1976,
	2217, 1661, 2217,
	1976, 1661, 1480, 1319
	};

// note durations: 4 = quarter note, 8 = eighth note, etc.:
int noteDurations[] = {
	8, 8, 8,
	8, 8, 8,
	10, 40, 40, 10, 8,
	8, 16, 16, 16, 16,

	40, 40, 10, 10, 8,
	8, 8, 8,
	8, 8, 8, 4
	};

void playMelody() {
  // iterate over the notes of the melody:
  for (int thisNote = 0; thisNote < 28; thisNote++) {

    // to calculate the note duration, take one second
    // divided by the note type.
    //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
    int noteDuration = 2000 / noteDurations[thisNote];
    tone(speakerPin, melody[thisNote], noteDuration);

    // to distinguish the notes, set a minimum time between them.
    // the note's duration + 30% seems to work well:
    int pauseBetweenNotes = noteDuration * 1.20;
    delay(pauseBetweenNotes);
    // stop the tone playing:
    noTone(speakerPin);
  }
}
