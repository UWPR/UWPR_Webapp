ALTER TABLE msData.msInstrument ADD COLUMN color VARCHAR(10);
INSERT INTO msData.msInstrument (name, description, active, color) VALUES ('Lumos', 'Thermo Lumos', 1, '800080');
UPDATE msData.msInstrument SET color='9932CC' WHERE id=2;
UPDATE msData.msInstrument SET color='8CBF40' WHERE id=3;
UPDATE msData.msInstrument SET color='1e90ff' WHERE id=4;
UPDATE msData.msInstrument SET color='8b008b' WHERE id=5;
UPDATE msData.msInstrument SET color='ffa500' WHERE id=6;
UPDATE msData.msInstrument SET color='20b2aa' WHERE id=7;
UPDATE msData.msInstrument SET color='dd4477' WHERE id=8;
UPDATE msData.msInstrument SET color='FF4500' WHERE id=9;
UPDATE msData.msInstrument SET color='4682B4' WHERE id=10;
UPDATE msData.msInstrument SET color='dc143c' WHERE id=11;
UPDATE msData.msInstrument SET color='e0c240' WHERE id=12;