-- ----------------------------
-- Table structure for T_PUB_TX_OUT_COMMAND
-- ----------------------------
DROP TABLE IF EXISTS `T_PUB_TX_OUT_COMMAND`;
CREATE TABLE `T_PUB_TX_OUT_COMMAND` (
  `ID` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key',
  `GLOBAL_TRANSACTION_ID` VARCHAR(255) COMMENT 'global transaction id,info only. Not unique, one global transaction may send multiple message',
  `TRANSACTION_TIME` TIMESTAMP COMMENT 'transaction generate time',
  `INSERT_BY` INT(11) DEFAULT -11 COMMENT 'Insert user id',
  `UPDATE_BY` INT(11) DEFAULT -11 COMMENT 'Latest update user id',
  `IS_RESPONDED` varchar(1) DEFAULT 'N' COMMENT 'Y/N/-, for external system which doesnot require response ,set to - directly',
  `TRANSACTION_TYPE` INT(11)  COMMENT 'transaction type',
  `LAST_RESEND_TIME` TIMESTAMP COMMENT 'last resend time',
  `RESEND_TIMES` INT(3)  COMMENT 'resend times',
  `RESPONSE_TIME` TIMESTAMP COMMENT 'target system response time',
  `NEED_RESPONSE` varchar(1) DEFAULT 'Y' COMMENT 'for external system may doesnot require response',
  `COMMAND_UUID` VARCHAR(36) COMMENT 'command uuid',
  `OUT_COMMAND_CLASS` VARCHAR(255) COMMENT 'command class',
  `ERROR_MESSAGE` VARCHAR(4000) COMMENT 'Error Message when send out command, the error message in target system will not be record here',
  `INSERT_TIME` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT  'Record insert time',
  `UPDATE_TIME` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record update time',
  `APP_NAME` VARCHAR(100) COMMENT 'sender application name',
  `COMMAND_DATA_1` VARCHAR(400) COMMENT 'command context 1',
  `COMMAND_DATA_2` VARCHAR(400) COMMENT 'command context 2',
  `COMMAND_DATA_3` VARCHAR(400) COMMENT 'command context 3',
  `COMMAND_DATA_4` VARCHAR(400) COMMENT 'command context 4',
  `COMMAND_DATA_5` VARCHAR(400) COMMENT 'command context 5',
  `FILE_PATH` VARCHAR(200) COMMENT 'file path for large command data'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;