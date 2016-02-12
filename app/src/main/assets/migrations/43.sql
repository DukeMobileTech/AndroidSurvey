ALTER TABLE Instruments ADD COLUMN CriticalMessage STRING;
ALTER TABLE InstrumentTranslations ADD COLUMN CriticalMessage STRING;
ALTER TABLE Questions ADD COLUMN Critical BOOLEAN;
ALTER TABLE Options ADD COLUMN Critical BOOLEAN;
ALTER TABLE Surveys ADD COLUMN CriticalResponses BOOLEAN;