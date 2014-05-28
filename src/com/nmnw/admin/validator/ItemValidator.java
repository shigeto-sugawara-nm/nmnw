package com.nmnw.admin.validator;

import java.util.List;

import com.nmnw.admin.validator.Validator;

public class ItemValidator {
	private static final String FIELD_NAME = "���i��";
	private static final String FIELD_PRICE = "���i�P��";
	private static final String FIELD_CATEGORY = "���i�W������";
	private static final String FIELD_IMAGE = "���i�摜";
	private static final String FIELD_EXPLANATION = "���i����";
	private static final String FIELD_SALES_PERIOD_FROM = "�̔��J�n��";
	private static final String FIELD_SALES_PERIOD_TO = "�̔��I����";
	private static final String FIELD_STOCK = "�݌ɐ�";
	private static final int MAX_SIZE_NAME = 200;
	private static final int MIN_SIZE_PRICE = 0;
	private static final int MAX_SIZE_PRICE = 10000;
	private static final int MAX_SIZE_EXPLANATION = 5000;
	private static final int MIN_SIZE_STOCK = 0;
	private static final int MAX_SIZE_STOCK = 100;

	private Validator v;
	
	/**
	 * Construct
	 */
	public ItemValidator() {
		v = new Validator();
	}

	public void checkName(String value) {
		if (!v.required(value, FIELD_NAME)) {
			v.maxSizeString(value, MAX_SIZE_NAME, FIELD_NAME);
		}
	}

	public void checkPrice(String value) {
		if (!v.required(value, FIELD_PRICE)) {
			if (!v.isInt(value, FIELD_PRICE)) {
				if (!v.minSizeInt(Integer.parseInt(value), MIN_SIZE_PRICE, FIELD_PRICE)) {
					v.maxSizeInt(Integer.parseInt(value), MAX_SIZE_PRICE, FIELD_PRICE);	
				}
			}
		}
	}

	public void checkCategory(String value) {
		v.requiredSelect(value, FIELD_CATEGORY);
	}

	public void checkExplanation(String value) {
		if (!v.required(value, FIELD_EXPLANATION)) {
			v.maxSizeString(value, MAX_SIZE_EXPLANATION, FIELD_EXPLANATION);
		}
	}

	public void checkSalesPeriodFrom(String value) {
		if (!v.required(value, FIELD_SALES_PERIOD_FROM)) {
			v.isDate(value, FIELD_SALES_PERIOD_FROM);
		}
	}

	public void checkSalesPeriodTo(String value) {
		if (!v.required(value, FIELD_SALES_PERIOD_TO)) {
			v.isDate(value, FIELD_SALES_PERIOD_TO);
		}
	}

	public void checkStock(String value) {
		if (!v.required(value, FIELD_STOCK)) {
			if (!v.isInt(value, FIELD_STOCK)) {
				if (!v.minSizeInt(Integer.parseInt(value), MIN_SIZE_STOCK, FIELD_STOCK)) {
					v.maxSizeInt(Integer.parseInt(value), MAX_SIZE_STOCK, FIELD_STOCK);
				}
			}
		}
	}

	public List<String> getValidationList() {
		return v.getErrorMessageList();
	}
}