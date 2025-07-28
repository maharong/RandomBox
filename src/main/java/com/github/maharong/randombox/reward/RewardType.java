package com.github.maharong.randombox.reward;

public enum RewardType {
    ITEM, // 아이템 지급
    MONEY, // 돈 지급 (Vault 연동)
    EXP, // 경험치 지급
    COMMAND, // 커맨드 발동
    BROADCAST, // 서버 전체 메세지
    MESSAGE // 개인 메세지
}
