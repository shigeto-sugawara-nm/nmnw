package com.nmnw.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nmnw.service.constant.ConfigConstants;
import com.nmnw.service.utility.DateConversionUtility;
import com.nmnw.service.utility.DdConnector;

public class OrderDao {
	private static final String TABLE_NAME_ORDER = "sales_order";
	private static final String TABLE_NAME_ORDER_DETAIL = "sales_order_detail";

	/**
	 * ��������(orderPeriod�w��)
	 * @param accountId
	 * @param orderPeriod
	 * @return List<Order>
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public List<Order> selectByAccountIdAndOrderPeriod(int accountId, String orderPeriod)
			throws ClassNotFoundException, SQLException {
		Connection connection = DdConnector.getConnection();
		// �������ԏ����L�����f�t���O
		Boolean hasOrderPeriod = false;
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select * from " + TABLE_NAME_ORDER + " where account_id = ?");
		// �������ԏ���������ꍇ
		if (orderPeriod != null && orderPeriod.length() > 0) {
			sqlBuilder.append(" and order_time > ?");
			hasOrderPeriod = true;
		}
		String sql = sqlBuilder.toString();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1, accountId);
		// �������ԏ���������ꍇ
		if (hasOrderPeriod) {
			statement.setString(2, orderPeriod);
		}
		ResultSet result = statement.executeQuery();
		List<Order> resultList = new ArrayList<Order>();
		while (result.next()) {
			Order order = new Order();
			order.setOrderId(result.getInt("order_id"));
			order.setOrderTime(DateConversionUtility.timestampToDate(result.getTimestamp("order_time")));
			order.setAccountId(result.getInt("account_id"));
			order.setAccountName(result.getString("account_name"));
			order.setAccountNameKana(result.getString("account_name_kana"));
			order.setAccountMail(result.getString("account_mail"));
			order.setAccountZipCode(result.getString("account_zip_code"));
			order.setAccountAddress(result.getString("account_address"));
			order.setAccountPhoneNumber(result.getString("account_phone_number"));
			order.setTotalPrice(result.getInt("total_price"));
			order.setCancelFlg(result.getBoolean("cancel_flg"));
			order.setCancelTime(DateConversionUtility.timestampToDate(result.getTimestamp("cancel_time")));
			order.setShippingFlg(result.getBoolean("shipping_flg"));
			order.setShippingTime(DateConversionUtility.timestampToDate(result.getTimestamp("shipping_time")));
 			resultList.add(order);
		}
		result.close();
		statement.close();
		connection.close();
		return resultList;
	}
}