package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/05 20:47
 * @FileName: SearchServiceImpl
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        /*
        ?????? ????????????dsl??????
        1. ?????????DSL?????? SearchRequest
        2. ??????DSL?????? SearchResponse searchResponse = client.search(searchRequest,RequestOptions.Default);
        3. ???????????????????????????????????? SearchResponseVo
         */
        // ??????????????????????????????
        SearchRequest searchRequest = this.buildDsl(searchParam);
        // ??????DSL??????
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // ???????????????????????????????????? SearchResponseVo
        SearchResponseVo searchResponseVo = this.parseResult(searchResponse);
        //        private List<SearchResponseTmVo> trademarkList;
        //        private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
        //        private List<Goods> goodsList = new ArrayList<>();
        //        private Long total;//????????????
        //  ---------------------------------------------------------
        //        private Integer pageSize;//?????????????????????
        //        private Integer pageNo;//????????????
        //        private Long totalPages;

        searchResponseVo.setPageSize(searchParam.getPageSize());
        searchResponseVo.setPageNo(searchParam.getPageNo());

        Long totalPages = (searchResponseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();
        searchResponseVo.setTotalPages(totalPages);

        return searchResponseVo;
    }

    /**
     * ??????????????????
     *
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        //        private Long total;//????????????
        SearchHits hits = searchResponse.getHits();
        searchResponseVo.setTotal(hits.getTotalHits().value);

        //        private List<Goods> goodsList = new ArrayList<>();
        ArrayList<Goods> goodsList = new ArrayList<>();
        SearchHit[] subHits = hits.getHits();
        if (subHits != null && subHits.length > 0) {
            for (SearchHit subHit : subHits) {
                String goodsString = subHit.getSourceAsString();
                Goods goods = JSON.parseObject(goodsString, Goods.class);
                if (subHit.getHighlightFields().get("title") != null) {
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    goods.setTitle(title.toString());
                }
                goodsList.add(goods);
            }
        }
        searchResponseVo.setGoodsList(goodsList);

        //        private List<SearchResponseTmVo> trademarkList;
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();

            String tmId = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(tmId));

            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);

            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);

        //        private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            String attrId = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseAttrVo.setAttrId(Long.parseLong(attrId));

            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            List<String> valueList = attrValueAgg.getBuckets()
                    .stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(valueList);

            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(attrsList);

        return searchResponseVo;
    }

    /**
     * ????????????DSL???????????????????????????
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildDsl(SearchParam searchParam) {
        // ???????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ??????????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // ??????Id
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        // ???????????????Id????????????
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                // ??????????????????bool
                BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
                // ???????????????bool
                BoolQueryBuilder innerBoolBuilder = QueryBuilders.boolQuery();
                // ????????????
                innerBoolBuilder.must(QueryBuilders.matchQuery("attrs.attrId", split[0]));
                innerBoolBuilder.must(QueryBuilders.matchQuery("attrs.attrValue", split[1]));
                // ???????????????
                boolBuilder.must(QueryBuilders.nestedQuery("attrs", innerBoolBuilder, ScoreMode.None));
                // ???????????????
                boolQueryBuilder.filter(boolBuilder);
                // ?????????????????????
                //boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", innerBoolBuilder, ScoreMode.None));
            }
        }
        // ??????
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }
        // keyword
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
            // ??????
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title").preTags("<span style=color:red>").postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
        // ??????
        searchSourceBuilder.from((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        searchSourceBuilder.size(searchParam.getPageSize());

        // ??????
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        } else {
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }
        // ??????
        // ???????????????????????????
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        // ??????????????????????????? ---- ???????????????nested
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        //  ??????dsl ?????????
        System.out.println("dsl:\t" + searchSourceBuilder.toString());
        //  ????????????field ???????????????????????????field ????????????????????????
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price"}, null);
        // ???????????????????????? GET /goods/_search ?????????????????? ?????????
        SearchRequest searchRequest = new SearchRequest("goods");
        // ???dsl ???????????????????????????
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    @Override
    public void incrHotScore(Long skuId) {
        String hotKey = "hotScore";
        Double count = this.redisTemplate.opsForZSet().incrementScore(hotKey, "skuId" + skuId, 1);
        if (count % 10 == 0) {
            // ??????es
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(count.longValue());
            this.goodsRepository.save(goods);
        }
    }

    // ???????????? --- ??????????????????Goods?????????Goods?????????ES???
    // ElasticsearchRepository
    @Override
    public void upperGoods(Long skuId) {
        // ????????????
        Goods goods = new Goods();
        SkuInfo skuInfo = this.productFeignClient.getSkuInfo(skuId);
        // ??????Id
        goods.setId(skuInfo.getId());
        // ????????????
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        // ????????????
        goods.setTitle(skuInfo.getSkuName());
        // ??????????????????
        goods.setPrice(this.productFeignClient.getSkuPrice(skuId).doubleValue());
        // ????????????
        goods.setCreateTime(new Date());
        // ????????????
        BaseTrademark trademark = this.productFeignClient.getTrademark(skuInfo.getTmId());
        goods.setTmId(trademark.getId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());
        // ????????????
        BaseCategoryView categoryView = this.productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());
        // ??????
        goods.setHotScore(0L);
        // ??????????????????
        List<BaseAttrInfo> attrList = this.productFeignClient.getAttrList(skuId);
        List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            // ??????sku??????????????????????????????????????????
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);
        // TODO ???????????????????????????
        this.goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

}
