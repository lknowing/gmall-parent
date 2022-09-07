package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/06 21:02
 * @FileName: ListController
 */
@Controller
public class ListController {
    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("list.html")
    public String list(SearchParam searchParam, Model model) {
        Result<Map> result = this.listFeignClient.search(searchParam);
        //  urlParam searchParam trademarkParam propsParamList orderMap 需要自己组装！
        String urlParam = this.makeUrlParam(searchParam);
        String trademarkParam = this.makeTrademarkParam(searchParam.getTrademark());
        List<SearchAttr> searchAttrList = this.makeSearchAttrList(searchParam.getProps());
        Map<String, Object> orderMap = this.makeOrderMap(searchParam.getOrder());
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("trademarkParam", trademarkParam);
        model.addAttribute("propsParamList", searchAttrList);
        model.addAttribute("orderMap", orderMap);
        //  trademarkList attrsList goodsList pageNo totalPages  这些数据都是实体类的属性 SearchResponseVo
        model.addAllAttributes(result.getData());
        return "list/index";
    }

    /**
     * @param order
     * @return
     */
    private Map<String, Object> makeOrderMap(String order) {
        HashMap<String, Object> map = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                map.put("type", split[0]);
                map.put("sort", split[1]);
            }
        } else {
            map.put("type", "1");
            map.put("sort", "desc");
        }
        return map;
    }

    /**
     * 平台属性面包屑集合
     *
     * @param props
     * @return
     */
    private List<SearchAttr> makeSearchAttrList(String[] props) {
        ArrayList<SearchAttr> searchAttrs = new ArrayList<>();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3) {
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(Long.parseLong(split[0]));
                    searchAttr.setAttrValue(split[1]);
                    searchAttr.setAttrName(split[2]);
                    searchAttrs.add(searchAttr);
                }
            }
        }
        return searchAttrs;
    }

    /**
     * 品牌面包屑
     *
     * @param trademark
     * @return
     */
    private String makeTrademarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return null;
    }

    /**
     * 记录用户通过哪些条件进行了检索
     *
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            stringBuilder.append("keyword:").append(searchParam.getKeyword());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            stringBuilder.append("category3Id:").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            stringBuilder.append("category2Id:").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            stringBuilder.append("category1Id:").append(searchParam.getCategory1Id());
        }
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&trademark:").append(trademark);
            }
        }
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("&prop:").append(prop);
                }
            }
        }
        return "list.html?" + stringBuilder.toString();
    }
}
