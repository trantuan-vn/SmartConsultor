-- MySQL Script generated by MySQL Workbench
-- Thu Jan 26 09:45:44 2023
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema core
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema core
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `core` DEFAULT CHARACTER SET utf8 ;
USE `core` ;

-- -----------------------------------------------------
-- Table `core`.`cTelegramBots`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cTelegramBots` (
  `id` INT NOT NULL,
  `tokenid` VARCHAR(46) NOT NULL COMMENT 'ví dụ: 5431606828:AAEDIenvthK1pkA0GBFhsLEp2sRgEiIjODY',
  `name` VARCHAR(30) NOT NULL,
  `description` VARCHAR(500) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `tokenid_UNIQUE` (`tokenid` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cProcesses`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cProcesses` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL COMMENT 'Tên được thêm .js để tạo ra file javascript nhằm chạy lệnh node',
  `status` TINYINT NOT NULL COMMENT 'Nếu process chạy để true, nếu process ko chạy để false',
  `command` VARCHAR(500) NOT NULL COMMENT 'forever start name.js',
  `description` VARCHAR(500) NOT NULL,
  `cTelegramBots_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE,
  INDEX `fk_cProcesses_cTelegramBots1_idx` (`cTelegramBots_id` ASC) VISIBLE,
  CONSTRAINT `fk_cProcesses_cTelegramBots1`
    FOREIGN KEY (`cTelegramBots_id`)
    REFERENCES `core`.`cTelegramBots` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cFunctions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cFunctions` (
  `id` INT NOT NULL,
  `name` VARCHAR(255) NOT NULL COMMENT 'Ví dụ: fs; child_process; common; ... \nNếu code bằng Null thì Require(\'name\')\nNếu code khác Null thì Require(\'./name.js\')',
  `code` LONGTEXT NULL,
  `description` VARCHAR(500) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cCommands`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cCommands` (
  `id` INT NOT NULL,
  `input` VARCHAR(255) NOT NULL COMMENT 'ví dụ: /initdb; /get symbols all; /get symbols FPT 5',
  `output` VARCHAR(1000) NOT NULL COMMENT 'chứa các sql khai thác các bảng trong database biến thành HTML. Ví dụ: select code, symbol, title, html from oSymbols',
  `cFunctions_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `input_UNIQUE` (`input` ASC) VISIBLE,
  INDEX `fk_cCommands_cFunctions1_idx` (`cFunctions_id` ASC) VISIBLE,
  CONSTRAINT `fk_cCommands_cFunctions1`
    FOREIGN KEY (`cFunctions_id`)
    REFERENCES `core`.`cFunctions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cProcesses_has_cCommands`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cProcesses_has_cCommands` (
  `cProcesses_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`cProcesses_id`, `cCommands_id`),
  INDEX `fk_cProcesses_has_cCommands_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  INDEX `fk_cProcesses_has_cCommands_cProcesses_idx` (`cProcesses_id` ASC) VISIBLE,
  CONSTRAINT `fk_cProcesses_has_cCommands_cProcesses`
    FOREIGN KEY (`cProcesses_id`)
    REFERENCES `core`.`cProcesses` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cProcesses_has_cCommands_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cFunctions_has_cFunctions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cFunctions_has_cFunctions` (
  `cFunctions_id` INT NOT NULL,
  `cFunctions_id1` INT NOT NULL,
  PRIMARY KEY (`cFunctions_id`, `cFunctions_id1`),
  INDEX `fk_cFunctions_has_cFunctions_cFunctions2_idx` (`cFunctions_id1` ASC) VISIBLE,
  INDEX `fk_cFunctions_has_cFunctions_cFunctions1_idx` (`cFunctions_id` ASC) VISIBLE,
  CONSTRAINT `fk_cFunctions_has_cFunctions_cFunctions1`
    FOREIGN KEY (`cFunctions_id`)
    REFERENCES `core`.`cFunctions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cFunctions_has_cFunctions_cFunctions2`
    FOREIGN KEY (`cFunctions_id1`)
    REFERENCES `core`.`cFunctions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cSysvar`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cSysvar` (
  `id` INT NOT NULL,
  `orderno` INT NOT NULL COMMENT 'thứ tự tham số đầu vào của functionid',
  `value` VARCHAR(255) NOT NULL COMMENT 'giá trị đầu vào dạng string',
  `description` VARCHAR(500) NULL,
  `cFunctions_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cSysvar_cFunctions1_idx` (`cFunctions_id` ASC) VISIBLE,
  CONSTRAINT `fk_cSysvar_cFunctions1`
    FOREIGN KEY (`cFunctions_id`)
    REFERENCES `core`.`cFunctions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cHistory`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cHistory` (
  `id` INT NOT NULL,
  `object` VARCHAR(255) NOT NULL,
  `history_date` DATETIME NOT NULL,
  `field` VARCHAR(255) NOT NULL,
  `type` INT NOT NULL COMMENT '1: created; 2: modified; 3: deleted',
  `value` TINYTEXT NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cErrorLog`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cErrorLog` (
  `id` INT NOT NULL,
  `log_date` DATETIME NOT NULL,
  `message` TINYTEXT NOT NULL,
  `cFunctions_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cErrorLog_cFunctions1_idx` (`cFunctions_id` ASC) VISIBLE,
  CONSTRAINT `fk_cErrorLog_cFunctions1`
    FOREIGN KEY (`cFunctions_id`)
    REFERENCES `core`.`cFunctions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cGroups`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cGroups` (
  `id` INT NOT NULL,
  `chatid` VARCHAR(14) NOT NULL,
  `package` INT NOT NULL COMMENT '1: basic; 2: vip; 3: supervip; 4: king; 5: diamond; 6: super diamond',
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cUsers`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cUsers` (
  `id` INT NOT NULL,
  `chatid` VARCHAR(10) NOT NULL,
  `status` INT NOT NULL DEFAULT 1 COMMENT '1: inactive; 2: active',
  `charge_date` DATETIME NOT NULL,
  `cash_balance` DECIMAL NOT NULL,
  `cGroups_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cUsers_cGroups1_idx` (`cGroups_id` ASC) VISIBLE,
  CONSTRAINT `fk_cUsers_cGroups1`
    FOREIGN KEY (`cGroups_id`)
    REFERENCES `core`.`cGroups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cGroupSchedules`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cGroupSchedules` (
  `id` INT NOT NULL,
  `starttime` VARCHAR(10) NOT NULL COMMENT 'ví dụ: \'9:15:00\'',
  `interval` VARCHAR(3) NOT NULL COMMENT 'ví dụ: 1h; 30m; 59s',
  `endtime` VARCHAR(10) NOT NULL COMMENT 'ví dụ: \'11:29:99\'',
  `sc` VARCHAR(7) NULL COMMENT 'daily; weekly; monthly',
  `weekly` VARCHAR(3) NULL COMMENT 'Mon, Tue, Wed, Thu, Fri, Sat, Sun',
  `monthly` INT NULL COMMENT 'Theo số tháng, ví dụ: 1-12',
  `not_run_on_holiday` INT NULL DEFAULT 0 COMMENT '0:false; 1:true',
  `cGroups_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cSchedules_cGroups1_idx` (`cGroups_id` ASC) VISIBLE,
  INDEX `fk_cSchedules_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_cSchedules_cGroups1`
    FOREIGN KEY (`cGroups_id`)
    REFERENCES `core`.`cGroups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cSchedules_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cHolidays`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cHolidays` (
  `id` INT NOT NULL,
  `dd` VARCHAR(2) NULL,
  `mm` VARCHAR(2) NULL,
  `yyyy` VARCHAR(4) NULL COMMENT 'Nếu là Null thì hàng năm lặp lại. \nNếu ngày nghỉ bất thường thì để khác Null',
  `description` VARCHAR(45) NULL,
  `cUsers_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cHolidays_cUsers1_idx` (`cUsers_id` ASC) VISIBLE,
  CONSTRAINT `fk_cHolidays_cUsers1`
    FOREIGN KEY (`cUsers_id`)
    REFERENCES `core`.`cUsers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`iDiscountUsers`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`iDiscountUsers` (
  `id` INT NOT NULL,
  `discount_rate` INT NOT NULL COMMENT 'ví dụ: 5 tg đg 5%',
  `month` INT NULL COMMENT 'tháng chiết khấu trong tương lai',
  `year` INT NULL COMMENT 'năm chiết khấu trong tương lai',
  `cUsers_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_iDiscountUsers_cUsers1_idx` (`cUsers_id` ASC) VISIBLE,
  CONSTRAINT `fk_iDiscountUsers_cUsers1`
    FOREIGN KEY (`cUsers_id`)
    REFERENCES `core`.`cUsers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cFees`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cFees` (
  `id` INT NOT NULL,
  `charge_money_per_a_month` DOUBLE NULL,
  `minimum_month_no` INT NULL,
  `cGroups_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cFees_cGroups1_idx` (`cGroups_id` ASC) VISIBLE,
  CONSTRAINT `fk_cFees_cGroups1`
    FOREIGN KEY (`cGroups_id`)
    REFERENCES `core`.`cGroups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cDiscountPolicies`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cDiscountPolicies` (
  `id` INT NOT NULL,
  `a_year_discount_rate` INT NOT NULL,
  `six_months_discount_rate` INT NOT NULL,
  `three_months_discount_rate` INT NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`iDeposits`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`iDeposits` (
  `id` INT NOT NULL,
  `transaction_date` DATETIME NOT NULL,
  `balance` DOUBLE NOT NULL,
  `full_message` VARCHAR(500) NOT NULL,
  `cUsers_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_iDeposits_cUsers1_idx` (`cUsers_id` ASC) VISIBLE,
  CONSTRAINT `fk_iDeposits_cUsers1`
    FOREIGN KEY (`cUsers_id`)
    REFERENCES `core`.`cUsers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`iCharges`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`iCharges` (
  `id` INT NOT NULL,
  `charge_date` DATETIME NULL,
  `balance` DOUBLE NULL,
  `cUsers_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_iCharges_cUsers1_idx` (`cUsers_id` ASC) VISIBLE,
  CONSTRAINT `fk_iCharges_cUsers1`
    FOREIGN KEY (`cUsers_id`)
    REFERENCES `core`.`cUsers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cUserSchedules`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`cUserSchedules` (
  `id` INT NOT NULL,
  `starttime` VARCHAR(10) NOT NULL COMMENT 'ví dụ: \'9:15:00\'',
  `interval` VARCHAR(3) NOT NULL COMMENT 'ví dụ: 1h; 30m; 59s',
  `endtime` VARCHAR(10) NOT NULL COMMENT 'ví dụ: \'11:29:99\'',
  `sc` VARCHAR(7) NULL COMMENT 'daily; weekly; monthly',
  `weekly` VARCHAR(3) NULL COMMENT 'Mon, Tue, Wed, Thu, Fri, Sat, Sun',
  `monthly` INT NULL COMMENT 'Theo số tháng, ví dụ: 1-12',
  `not_run_on_holiday` INT NULL DEFAULT 0 COMMENT '0:false; 1:true',
  `cCommands_id` INT NOT NULL,
  `cUsers_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_cSchedules_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  INDEX `fk_cGroupSchedules_copy1_cUsers1_idx` (`cUsers_id` ASC) VISIBLE,
  CONSTRAINT `fk_cSchedules_cCommands10`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cGroupSchedules_copy1_cUsers1`
    FOREIGN KEY (`cUsers_id`)
    REFERENCES `core`.`cUsers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oSymbols`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oSymbols` (
  `id` INT NOT NULL,
  `symbol_code` VARCHAR(45) NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_oSymbols_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_oSymbols_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oSymbolGroups`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oSymbolGroups` (
  `id` INT NOT NULL,
  `group_code` VARCHAR(45) NULL,
  `cCommands_id` INT NOT NULL,
  `type` INT NULL COMMENT '1:fireant; 2: HSX; 3: HNX; 4: VFM ETF Diamond',
  PRIMARY KEY (`id`),
  INDEX `fk_oSymbols_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_oSymbols_cCommands10`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oSymbols_has_oSymbolGroups`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oSymbols_has_oSymbolGroups` (
  `oSymbols_id` INT NOT NULL,
  `oSymbolGroups_id` INT NOT NULL,
  PRIMARY KEY (`oSymbols_id`, `oSymbolGroups_id`),
  INDEX `fk_oSymbols_has_oSymbolGroups_oSymbolGroups1_idx` (`oSymbolGroups_id` ASC) VISIBLE,
  INDEX `fk_oSymbols_has_oSymbolGroups_oSymbols1_idx` (`oSymbols_id` ASC) VISIBLE,
  CONSTRAINT `fk_oSymbols_has_oSymbolGroups_oSymbols1`
    FOREIGN KEY (`oSymbols_id`)
    REFERENCES `core`.`oSymbols` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oSymbols_has_oSymbolGroups_oSymbolGroups1`
    FOREIGN KEY (`oSymbolGroups_id`)
    REFERENCES `core`.`oSymbolGroups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oSymbolRRGs`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oSymbolRRGs` (
  `id` INT NOT NULL,
  `input_date` DATETIME NOT NULL,
  `rs` DOUBLE NOT NULL,
  `rm` DOUBLE NOT NULL,
  `oSymbols_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_oSymbolRRGs_oSymbols1_idx` (`oSymbols_id` ASC) VISIBLE,
  INDEX `fk_oSymbolRRGs_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_oSymbolRRGs_oSymbols1`
    FOREIGN KEY (`oSymbols_id`)
    REFERENCES `core`.`oSymbols` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oSymbolRRGs_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oQuarterFinancialReports`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oQuarterFinancialReports` (
  `id` INT NOT NULL,
  `fr_orderno` INT NULL,
  `fr_id` INT NULL,
  `fr_name` VARCHAR(45) NULL,
  `fr_parentid` INT NULL,
  `fr_level` INT NULL,
  `fr_year` INT NULL,
  `fr_quarter` INT NULL,
  `fr_value` DOUBLE NULL,
  `fr_type` INT NULL COMMENT '1: balance sheet; 2: income; 3: lltt truc tiep; 4: lltt gian tiep',
  `oSymbols_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_oQuarterFinancialReports_oSymbols1_idx` (`oSymbols_id` ASC) VISIBLE,
  INDEX `fk_oQuarterFinancialReports_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_oQuarterFinancialReports_oSymbols1`
    FOREIGN KEY (`oSymbols_id`)
    REFERENCES `core`.`oSymbols` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oQuarterFinancialReports_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oSymbolEvents`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oSymbolEvents` (
  `id` INT NOT NULL,
  `type` INT NULL COMMENT '1: bằng tiền; 2: bằng CP; 3: quyền mua',
  `title` VARCHAR(45) NULL,
  `record_date` DATETIME NULL COMMENT 'ngày giao dịch không hưởng quyền \n',
  `registration_date` DATETIME NULL COMMENT 'ngày đăng ký cuối cùng',
  `execution_date` DATETIME NULL COMMENT 'ngày thực hiện',
  `oSymbols_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_oSymbolEvents_oSymbols1_idx` (`oSymbols_id` ASC) VISIBLE,
  INDEX `fk_oSymbolEvents_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_oSymbolEvents_oSymbols1`
    FOREIGN KEY (`oSymbols_id`)
    REFERENCES `core`.`oSymbols` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oSymbolEvents_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oFinancialReportPath`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oFinancialReportPath` (
  `id` INT NOT NULL,
  `quarter` INT NOT NULL,
  `year` INT NOT NULL,
  `file_path` VARCHAR(255) NOT NULL,
  `oSymbols_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_oFinancialReportPath_oSymbols1_idx` (`oSymbols_id` ASC) VISIBLE,
  INDEX `fk_oFinancialReportPath_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  CONSTRAINT `fk_oFinancialReportPath_oSymbols1`
    FOREIGN KEY (`oSymbols_id`)
    REFERENCES `core`.`oSymbols` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oFinancialReportPath_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`iCompanies`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`iCompanies` (
  `id` INT NOT NULL,
  `user_account` VARCHAR(45) NULL COMMENT 'VD: 001C123456',
  `account_password` VARCHAR(45) NULL COMMENT 'mật khẩu TKGD\n',
  `home_page` VARCHAR(45) NULL COMMENT 'VD: www.ssi.com.vn',
  `code` VARCHAR(45) NULL COMMENT 'VD: SSI, ACBS,...\\',
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`oSymbolReportFromCompanies`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `core`.`oSymbolReportFromCompanies` (
  `id` INT NOT NULL,
  `title` VARCHAR(255) NULL,
  `report_date` DATETIME NULL,
  `file_path` VARCHAR(255) NULL,
  `oSymbolReportFromCompaniescol` VARCHAR(45) NULL,
  `oSymbols_id` INT NOT NULL,
  `cCommands_id` INT NOT NULL,
  `iCompanies_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_oSymbolReportFromCompanies_oSymbols1_idx` (`oSymbols_id` ASC) VISIBLE,
  INDEX `fk_oSymbolReportFromCompanies_cCommands1_idx` (`cCommands_id` ASC) VISIBLE,
  INDEX `fk_oSymbolReportFromCompanies_iCompanies1_idx` (`iCompanies_id` ASC) VISIBLE,
  CONSTRAINT `fk_oSymbolReportFromCompanies_oSymbols1`
    FOREIGN KEY (`oSymbols_id`)
    REFERENCES `core`.`oSymbols` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oSymbolReportFromCompanies_cCommands1`
    FOREIGN KEY (`cCommands_id`)
    REFERENCES `core`.`cCommands` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_oSymbolReportFromCompanies_iCompanies1`
    FOREIGN KEY (`iCompanies_id`)
    REFERENCES `core`.`iCompanies` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;