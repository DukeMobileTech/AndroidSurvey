ALTER TABLE EventLog ADD COLUMN InstrumentRemoteId LONG;
ALTER TABLE Grids ADD COLUMN InstrumentRemoteId LONG;
ALTER TABLE InstrumentTranslations ADD COLUMN InstrumentRemoteId LONG;
ALTER TABLE Questions ADD COLUMN InstrumentRemoteId LONG;
ALTER TABLE Rules ADD COLUMN InstrumentRemoteId LONG;
ALTER TABLE Sections ADD COLUMN InstrumentRemoteId LONG;
ALTER TABLE Surveys ADD COLUMN InstrumentRemoteId LONG;