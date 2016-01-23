#include <LiquidCrystal.h>
#include <RFduinoBLE.h>

LiquidCrystal lcd(1, 2, 3, 4, 5, 6);

void setup() {
  RFduinoBLE.deviceName="lcd_BLE";
  RFduinoBLE.advertisementData="lcd";
  
  // set LCD cursor
  lcd.setCursor(0,1);
  lcd.begin(16,2);
  
  // start the BLE stack
  RFduinoBLE.begin();
}

void loop() {
  //switch to sleep mode
  RFduino_ULPDelay(INFINITE);
}

void RFduinoBLE_onAdvertisement(bool start){
  if( start ) {
    lcd.clear();
    lcd.print("ad-ing..");
  } else {
    lcd.clear();
    lcd.print("ad finish");
  }
}

void RFduinoBLE_onConnect(){
  lcd.clear();
  lcd.print("connected");
}

void RFduinoBLE_onDisconnect(){
  lcd.clear();
  lcd.print("disconn.");
}

void RFduinoBLE_onReceive(char* data, int len){
  lcd.clear(); lcd.print("data received");
  lcd.setCursor(0,2); lcd.print(data);
}
