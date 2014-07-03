package com.nmnw.admin.function.item.New;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.nmnw.admin.dao.Item;
import com.nmnw.admin.dao.ItemDao;
import com.nmnw.admin.utility.DateConversionUtility;
import com.nmnw.admin.utility.ExceptionUtility;
import com.nmnw.admin.utility.FileUtility;
import com.nmnw.admin.validator.ItemValidator;
import com.nmnw.admin.constant.ConfigConstants;
import static com.nmnw.admin.utility.PropertyUtility.getPropertyValue;

@WebServlet(name="admin/item/new", urlPatterns={"/admin/item/new"})
@MultipartConfig(location = ConfigConstants.TMP_IMAGE_DIR)
public class NewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct
	 */
	public NewServlet () {
		super();
	}

	@Override
	protected void doGet (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		List<String> errorMessageList = new ArrayList<String>();
		Map<String, String[]> inputDataList = request.getParameterMap();
		String action = request.getParameter("action");
		String page = ConfigConstants.JSP_DIR_ITEM_NEW + "New.jsp";
		// 入力画面表示
		if (!("new_end".equals(action))) {
			errorMessageList.add("");
			request.setAttribute("errorMessageList", errorMessageList);
			request.setAttribute("inputDataList", inputDataList);
			request.getRequestDispatcher(page).forward(request, response);
		} else {
		// 新規登録
			// validation
			ItemValidator iv = new ItemValidator();
			iv.checkName(request.getParameter("item_name"));
			iv.checkPrice(request.getParameter("item_price"));
			iv.checkCategory(request.getParameter("item_category"));
			iv.checkExplanation(request.getParameter("item_explanation"));
			iv.checkSalesPeriodFrom(request.getParameter("item_sales_period_from"));
			iv.checkSalesPeriodTo(request.getParameter("item_sales_period_to"));
			iv.checkStock(request.getParameter("item_stock"));
			Part image = request.getPart("item_image");
			iv.checkImage(image);

			errorMessageList = iv.getValidationList();
			// 入力エラーの場合
			if (errorMessageList.size() != 0) {
				request.setAttribute("errorMessageList", errorMessageList);
				request.setAttribute("inputDataList", inputDataList);
				request.getRequestDispatcher(page).forward(request, response);
			} else {
				try {
					// set to data object
					Item item = new Item();
					item.setName(request.getParameter("item_name"));
					item.setPrice(Integer.parseInt(request.getParameter("item_price")));
					item.setCategory(request.getParameter("item_category"));
					item.setExplanation(request.getParameter("item_explanation"));
					item.setSalesPeriodFrom(DateConversionUtility.stringToDate(request.getParameter("item_sales_period_from")));
					item.setSalesPeriodTo(DateConversionUtility.stringToDate(request.getParameter("item_sales_period_to")));
					item.setStock(Integer.parseInt(request.getParameter("item_stock")));
					String newImageFileName = FileUtility.getNewFileName(image, "item");
					image.write(getPropertyValue("STORED_IMAGE_DIR_ITEM") + newImageFileName);
					item.setImageUrl(newImageFileName);

					ItemDao itemdao = new ItemDao();
					String itemId = String.valueOf(itemdao.insert(item));
					String url = "http://" + getPropertyValue("DOMAIN") + ConfigConstants.SERVLET_DIR_ITEM_DETAIL + "?item_id=" + itemId + "&action=new_end";
					response.sendRedirect(url);
				} catch (Exception e) {
					e.printStackTrace();
					ExceptionUtility.redirectErrorPage(request, response, e);
				}
			}
		}
	}

	@Override
	protected void doPost (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doGet(request, response);
	}
}