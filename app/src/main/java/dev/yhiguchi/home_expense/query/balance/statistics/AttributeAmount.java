package dev.yhiguchi.home_expense.query.balance.statistics;

/** 属性ごとの金額 */
public record AttributeAmount(String attributeId, String attributeName, long amount) {}
