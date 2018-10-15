package be.vdab.demo.shopfront.services;

import be.vdab.demo.shopfront.model.Product;
import be.vdab.demo.shopfront.services.dto.ProductDTO;
import be.vdab.demo.shopfront.services.dto.StockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RibbonClients({@RibbonClient(name = "productcatalogue"), @RibbonClient(name = "stockmanager")})
public class ProductService {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private RestTemplate restTemplate;

    public List<Product> getProducts() {
        final Map<String, ProductDTO> productDTOs = getProductDTOs();
        final Map<String, StockDTO> stockDTOMap = getStockDTOs();

        // Merge productDTOs and stockDTOs to a List of Products
        return productDTOs.values().stream()
                .map(productDTO -> {
                    StockDTO stockDTO = stockDTOMap.get(productDTO.getId());
                    if (stockDTO == null) {
                        stockDTO = StockDTO.DEFAULT_STOCK_DTO;
                    }
                    return new Product(productDTO.getId(), stockDTO.getSku(), productDTO.getName(), productDTO.getDescription(), productDTO.getPrice(), stockDTO.getAmountAvailable());
                })
                .collect(Collectors.toList());
    }

    private Map<String, ProductDTO> getProductDTOs() {
        ResponseEntity<List<ProductDTO>> productCatalogueResponse =
                restTemplate.exchange("http://shop-gateway:80/productcatalogue/products/v1.0",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<ProductDTO>>() {
                        });
        List<ProductDTO> productDTOs = productCatalogueResponse.getBody();

        return productDTOs.stream()
                .collect(Collectors.toMap(ProductDTO::getId, Function.identity()));
    }

    //    @HystrixCommand(fallbackMethod = "stocksNotFound") // Hystrix circuit breaker for fault-tolernace demo
    private Map<String, StockDTO> getStockDTOs() {
        ResponseEntity<List<StockDTO>> stockManagerResponse =
                restTemplate.exchange("http://shop-gateway:80/stockmanager/stocks/v1.0",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<StockDTO>>() {
                        });
        List<StockDTO> stockDTOs = stockManagerResponse.getBody();

        return stockDTOs.stream()
                .collect(Collectors.toMap(StockDTO::getProductId, Function.identity()));
    }

}
