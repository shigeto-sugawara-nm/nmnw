package com.nmnw.service.function.account.resetPassword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nmnw.service.constant.ConfigConstants;
import com.nmnw.service.constant.MessageConstants;
import com.nmnw.service.utility.CipherUtility;
import com.nmnw.service.utility.DateConversionUtility;
import com.nmnw.service.utility.ExceptionUtility;
import com.nmnw.service.utility.MailUtility;
import com.nmnw.service.utility.RandomStringUtility;
import com.nmnw.service.validator.AccountValidator;
import com.nmnw.service.dao.Account;
import com.nmnw.service.dao.AccountDao;
import com.nmnw.service.dao.Mail;
import com.nmnw.service.dao.MailDao;

@WebServlet(name="account/resetPassword", urlPatterns={"/account/resetPassword"})
public class ResetPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// subject
	private static final String CHANGE_PASSWORD_URL = "http://localhost:8080/nmnw/account/changePassword?action=edit&token=";
	private static final String MAIL_CODE = "reset_password";
	private static final String DISPLAY_TITLE_ERROR = "エラー";
	private static final String DISPLAY_TITLE_RESET = "リセット";
	private static final String DISPLAY_TITLE_RESET_END = "リセット完了";
	private static final String DISPLAY_TITLE_EDIT = "変更";
	private static final String DISPLAY_TITLE_EDIT_END = "変更完了";
	
	/**
	 * Construct
	 */
	public ResetPasswordServlet () {
		super();
	}

	@Override
	protected void doGet (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		String page = ConfigConstants.JSP_DIR_ACCOUNT_RESET_PASSWORD + "ResetPassword.jsp";
		String action = request.getParameter("action");
		String mailTo = request.getParameter("mail");
		String token = request.getParameter("token");
		List<String> errorMessageList = new ArrayList<String>();
		Calendar currentDateTime = Calendar.getInstance();

		try {
			// actionパラメータがない、または意図しない値の場合
			String[] vaildActionParam = {"reset", "reset_end", "edit", "edit_end"};
			if (action == null || !Arrays.asList(vaildActionParam).contains(action)) {
				// エラー
				errorMessageList.add(MessageConstants.MESSAGE_ILLEGAL_PARAMETER);
				request.setAttribute("errorMessageList", errorMessageList);
				request.setAttribute("title", DISPLAY_TITLE_ERROR);
				request.getRequestDispatcher(page).forward(request, response);
				return;
			}
			////////////////////////////
			// パスワード変更用メール送信画面表示
			////////////////////////////
			if ("reset".equals(action)) {
				request.setAttribute("mail", mailTo);
				request.setAttribute("action", "reset");
				request.setAttribute("title", DISPLAY_TITLE_RESET);
				request.getRequestDispatcher(page).forward(request, response);
				return;
			}
			////////////////////////////
			// パスワード変更用メール送信
			////////////////////////////
			if ("reset_end".equals(action)) {
				// 入力チェック
				AccountValidator av = new AccountValidator();
				av.checkMail(request.getParameter("mail"));
	
				errorMessageList = av.getValidationList();
				// 入力エラーの場合
				if (errorMessageList.size() != 0) {
					// エラー
					request.setAttribute("action", "reset");
					request.setAttribute("errorMessageList", errorMessageList);
					request.setAttribute("title", DISPLAY_TITLE_RESET);
					request.getRequestDispatcher(page).forward(request, response);
					return;
				}
				// 入力チェックOKの場合
				// 入力メールよりアカウント情報取得
				AccountDao accountDao = new AccountDao();
				Account account = accountDao.selectByMail(request.getParameter("mail"));
				if (account.getId() == 0) {
					// エラー
					request.setAttribute("action", "reset");
					errorMessageList.add(MessageConstants.MESSAGE_MAIL_NOT_EXIST);
					request.setAttribute("errorMessageList", errorMessageList);
					request.setAttribute("title", DISPLAY_TITLE_RESET);
					request.getRequestDispatcher(page).forward(request, response);
					return;
				}
				// パスワード変更用ｔｏｋｅｎ生成
				token = RandomStringUtility.generateToken();
				// token有効期限
				Date tokenExpireTime = DateConversionUtility.getdaysAfterDate(ConfigConstants.TOKEN_EXPIRE_DAYS);
				Account updateAccount = new Account();
				updateAccount.setId(account.getId());
				updateAccount.setToken(token);
				updateAccount.setTokenExpireTime(tokenExpireTime);
				// DB格納
				int updateCount = accountDao.update(updateAccount);

				// メール送信
				MailDao mailDao = new MailDao();
				Mail mail = mailDao.selectByCode(MAIL_CODE);
				String message = mail.getMessage().replace("{Url}", CHANGE_PASSWORD_URL + token);

				boolean sendResult = MailUtility.sendMail(mailTo, mail.getSubject(), message);
				if (sendResult == false) {
					errorMessageList.add(MessageConstants.ERROR_SEND_MAIL);
				}
				request.setAttribute("errorMessageList", errorMessageList);
				request.setAttribute("action", "reset_end");
				request.setAttribute("title", DISPLAY_TITLE_RESET_END);
				request.getRequestDispatcher(page).forward(request, response);
				return;
			}
			////////////////////////////
			// パスワード変更用画面表示
			////////////////////////////
			if ("edit".equals(action)) {
				// tokenパラメータチェック
				Account account = getAccountByToken(token, currentDateTime);
				if (account == null) {
					// エラー
					errorMessageList.add(MessageConstants.MESSAGE_ILLEGAL_PARAMETER);
					request.setAttribute("errorMessageList", errorMessageList);
					request.setAttribute("title", DISPLAY_TITLE_ERROR);
					request.getRequestDispatcher(page).forward(request, response);
					return;
				}
				request.setAttribute("token", token);
				request.setAttribute("action", "edit");
				request.setAttribute("title", DISPLAY_TITLE_EDIT);
				request.getRequestDispatcher(page).forward(request, response);
				return;
			}
			////////////////////////////
			// パスワード変更
			////////////////////////////
			if ("edit_end".equals(action)) {
				// tokenパラメータチェック
				Account account = getAccountByToken(token, currentDateTime);
				if (account == null) {
					// エラー
					errorMessageList.add(MessageConstants.MESSAGE_ILLEGAL_PARAMETER);
					request.setAttribute("errorMessageList", errorMessageList);
					request.setAttribute("title", DISPLAY_TITLE_ERROR);
					request.getRequestDispatcher(page).forward(request, response);
					return;
				}
				// 入力チェック
				AccountValidator av = new AccountValidator();
				av.checkPassWord(request.getParameter("password"));
				av.checkPassWord(request.getParameter("retype_password"));
				av.checkPassWordAndRetypePassWord(request.getParameter("password"), request.getParameter("retype_password"));

				errorMessageList = av.getValidationList();
				// 入力エラーの場合
				if (errorMessageList.size() != 0) {
					// 再度変更画面表示
					request.setAttribute("token", token);
					request.setAttribute("action", "edit");
					request.setAttribute("errorMessageList", errorMessageList);
					request.setAttribute("title", DISPLAY_TITLE_EDIT);
					request.getRequestDispatcher(page).forward(request, response);
					return;
				}
				// 変更
				// salt生成
				String salt = RandomStringUtility.generateSalt();
				// 入力パスワードとsaltを組み合わせてハッシュ化
				String enctyptPassword = CipherUtility.enctypt(request.getParameter("password") + salt);
				Account updateAccount = new Account();
				updateAccount.setId(account.getId());
				updateAccount.setPassWord(enctyptPassword);
				updateAccount.setSalt(salt);
				// update
				AccountDao accountDao = new AccountDao();
				int accountId = accountDao.update(updateAccount);
				// 正常に1件更新されていた場合
				if (accountId == 1) {
					request.setAttribute("action", "edit_end");
					request.setAttribute("title", DISPLAY_TITLE_EDIT_END);
					request.getRequestDispatcher(page).forward(request, response);
					return;
				}
				// エラー
				errorMessageList.add(MessageConstants.MESSAGE_CHANGE_PASSWORD_FAILED);
				request.setAttribute("errorMessageList", errorMessageList);
				request.setAttribute("title", DISPLAY_TITLE_ERROR);
				request.getRequestDispatcher(page).forward(request, response);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionUtility.redirectErrorPage(request, response, e);
		}
	}

	@Override
	protected void doPost (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doGet(request, response);
	}

	/**
	 * actionパラメータチェック
	 * @param token
	 * @return Account
	 */
	private Account getAccountByToken (String token, Calendar currentDateTime) {
		if (token == null) {
			return null;
		}
		try {
			// 会員情報取得
			AccountDao accountDao = new AccountDao();
			Account account = accountDao.selectByTokenAndTokenExpireTime(token, currentDateTime);
			// tokenが不正
			if (account == null) {
				// エラー
				return null;
			}
			return account;
		} catch (Exception e) {
			return null;
		}
	}
}