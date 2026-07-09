-- Add online_status column to user table for presence tracking
ALTER TABLE `user` ADD COLUMN `online_status` VARCHAR(20) DEFAULT 'online' COMMENT '在线状态: online/busy/away/invisible';
