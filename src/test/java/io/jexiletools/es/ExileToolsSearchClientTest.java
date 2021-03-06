/*
 * Copyright (C) 2015 thirdy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package io.jexiletools.es;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import io.jexiletools.es.model.json.ExileToolsHit;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;

/**
 * @author thirdy
 *
 */
public class ExileToolsSearchClientTest {
	
	private static final String BLACK_MARKET_API_KEY = "4b1ccf2fce44441365118e9cd7023c38";

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	static ExileToolsSearchClient client;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = new ExileToolsSearchClient("http://api.exiletools.com/index", BLACK_MARKET_API_KEY);
	}
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		client.shutdown();
	}
	
	// TODO: we've excluded most of ES's transitve deps, however this breaks these tests when running with mvn test
	
	@Test
	public void testGrabItemMapping() throws Exception {
		HttpResponse<String> httpResponse = Unirest.get("http://api.exiletools.com/index/_mapping")
			.header("Authorization", "DEVELOPMENT-Indexer")
			.asString();
		String body = httpResponse.getBody();
		File mapping = new File("poe-mapping.json");
		FileUtils.writeStringToFile(mapping, body, Charsets.UTF_8);
	}
	
	@Test
	@Ignore
	public void testQuery() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	    FilterBuilder filter = FilterBuilders.boolFilter()
	            .must(FilterBuilders.termFilter("attributes.league", "Flashback Event HC (IC002)"))
	            .must(FilterBuilders.termFilter("attributes.equipType", "Jewel"))
	            .must(FilterBuilders.rangeFilter("modsTotal.#% increased maximum Life").gt(4))
	            .must(FilterBuilders.termFilter("shop.verified", "yes"))
	           // .must(FilterBuilders.termFilter("attributes.rarity", "Magic"))
	            ;

	    searchSourceBuilder
	            .query(QueryBuilders.filteredQuery(QueryBuilders.boolQuery().minimumNumberShouldMatch(2)
	                    .should(QueryBuilders.rangeQuery("modsTotal.#% increased Area Damage"))
	                    .should(QueryBuilders.rangeQuery("modsTotal.#% increased Projectile Damage"))
	                    .should(QueryBuilders.rangeQuery("modsTotal.#% increased Chaos Damage")), filter))
	            .sort("_score");
	    SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		List<Hit<ExileToolsHit, Void>> hits = result.getHits(ExileToolsHit.class);
		hits.stream().map(hit -> hit.source).forEach(System.out::println);
	}
	
	/**
	 * As per ES documentation/tome, the best way to do our search is via Filters
	 */
	@Test
	@Ignore
	public void testExecuteMjolnerUsingFilters() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		List<FilterBuilder> filters = new LinkedList<>();
		
		filters.add(FilterBuilders.termFilter("attributes.league", "Flashback Event (IC001)"));
//		filters.add(FilterBuilders.termFilter("info.name", "Mjolner"));
		filters.add(FilterBuilders.termFilter("info.name", "Hegemony's Era"));
		filters.add(FilterBuilders.rangeFilter("properties.Weapon.Physical DPS").from(400));
		
		FilterBuilder filter = FilterBuilders.andFilter(filters.toArray(new FilterBuilder[filters.size()]));
		searchSourceBuilder.query(QueryBuilders.filteredQuery(null, filter));
		searchSourceBuilder.size(100);
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		List<Hit<ExileToolsHit, Void>> hits = result.getHits(ExileToolsHit.class);
		for (Hit<ExileToolsHit, Void> hit : hits) {
//			logger.info(hit.source.toString());
//			hit.source.getQuality().ifPresent( q -> logger.info(q.toString()) );
			hit.source.getPhysicalDPS().ifPresent( q -> logger.info(q.toString()) );
//			logger.info(hit.source.toString());
//			logger.info(hit.source.getRequirements().getLevel().toString());
//			logger.info(hit.source.getExplicitMods().toString());
		}
	}
	
	@Test
	@Ignore
	public void testDistinctCurrencyIconValues() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.aggregation(AggregationBuilders.terms("rarities").field("shop.verified")
				.size(0));
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		logger.info(result.getJsonString());
		System.out.println("-------");
		result.getAggregations().getTermsAggregation("rarities").getBuckets().stream()
			.map(e -> e.getKey())
			.sorted()
			.filter(k -> k.contains("Currency"))
			.forEach(k -> System.out.println(k));
	}
	
	@Test
	@Ignore
	public void testDistinctItemTypeValues() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.filtered(null, FilterBuilders.boolFilter()
				.must(FilterBuilders.termFilter("attributes.itemType", "Card"))
				));
		searchSourceBuilder.aggregation(AggregationBuilders.terms("rarities").field("info.name")
				.size(0));
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		logger.info(result.getJsonString());
		System.out.println("-------");
		result.getAggregations().getTermsAggregation("rarities").getBuckets().stream()
		.map(e -> e.getKey())
		.sorted()
		.forEach(k -> System.out.println(k));
	}

	@Test
	@Ignore
	public void testExecuteMjolner() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		 searchSourceBuilder.query(QueryBuilders.matchQuery("info.name", "Mjolner"));
		 searchSourceBuilder.size(1);
		
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		List<Hit<ExileToolsHit, Void>> hits = result.getHits(ExileToolsHit.class);
		for (Hit<ExileToolsHit, Void> hit : hits) {
			logger.info(hit.source.toString());
		}
	}
	
	@Test
	@Ignore
	public void testExecuteTabula() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("info.name", "Tabula Rasa"));
		searchSourceBuilder.size(1);
		
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		List<Hit<ExileToolsHit, Void>> hits = result.getHits(ExileToolsHit.class);
		for (Hit<ExileToolsHit, Void> hit : hits) {
			logger.info(hit.source.toString());
		}
	}
	
	@Test
	@Ignore
	public void testShops() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("attributes.league", "Flashback Event (IC001)"))
				.mustNot(QueryBuilders.matchQuery("attributes.league", "Flashback Event (IC001)")
						));
		searchSourceBuilder.size(1);
		
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		List<Hit<ExileToolsHit, Void>> hits = result.getHits(ExileToolsHit.class);
		for (Hit<ExileToolsHit, Void> hit : hits) {
			logger.info(hit.source.toString());
		}
	}
	
	@Test
	@Ignore
	public void testGetLeagues() throws Exception {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.aggregation(AggregationBuilders
				.terms("leagues")
				.field("attributes.league"));
//		searchSourceBuilder.size(0);
		
		SearchResult result = client.execute(searchSourceBuilder.toString()).getSearchResult();
		List<Hit<ExileToolsHit, Void>> hits = result.getHits(ExileToolsHit.class);
		for (Hit<ExileToolsHit, Void> hit : hits) {
			logger.info(hit.source.toString());
		}
	}

}
