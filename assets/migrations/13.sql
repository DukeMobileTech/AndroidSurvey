ALTER TABLE AdminSettings ADD COLUMN ShowSkip BOOLEAN;
ALTER TABLE AdminSettings ADD COLUMN ShowNA BOOLEAN;
ALTER TABLE AdminSettings ADD COLUMN ShowRF BOOLEAN;
ALTER TABLE AdminSettings ADD COLUMN ShowDK BOOLEAN;
CREATE TABLE Images (RemoteId LONG, PhotoUrl STRING, Question QUESTION);
