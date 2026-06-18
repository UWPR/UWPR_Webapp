# --------------------------------------------------------------------------------
# NOTICE TABLE
# Date-bounded announcements shown on the home page (front.jsp)
# --------------------------------------------------------------------------------
CREATE TABLE notice
(
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    startDate   DATE NOT NULL,
    endDate     DATE NOT NULL,
    message     TEXT NOT NULL,
    createdBy   INT UNSIGNED NOT NULL,
    createDate  DATETIME NOT NULL,
    INDEX idx_notice_dates (startDate, endDate)
);
