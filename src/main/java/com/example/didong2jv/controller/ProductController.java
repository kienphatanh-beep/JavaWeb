package com.example.didong2jv.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.didong2jv.config.AppConstants;
import com.example.didong2jv.entity.Product;
import com.example.didong2jv.payloads.ProductDTO;
import com.example.didong2jv.payloads.ProductResponse;
import com.example.didong2jv.service.ProductService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
@CrossOrigin(origins = "*", exposedHeaders = "X-Total-Count")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ========================================================================
    // 🔥 HÀM HỖ TRỢ HATEOAS: Chuyển DTO sang EntityModel kèm Links
    // ========================================================================
    private EntityModel<ProductDTO> toModel(ProductDTO dto) throws IOException {
        return EntityModel.of(dto,
            linkTo(methodOn(ProductController.class).getProductById(dto.getProductId())).withSelfRel(),
            linkTo(methodOn(ProductController.class).getImage(dto.getImage())).withRel("image"),
            linkTo(methodOn(ProductController.class).getAllProducts(
                AppConstants.PAGE_NUMBER, 
                AppConstants.PAGE_SIZE, 
                AppConstants.SORT_PRODUCTS_BY, 
                AppConstants.SORT_DIR)).withRel("all-products")
        );
    }

    // ========================================================================
    // 1. API TÌM KIẾM TỔNG HỢP (HATEOAS) DL Thô -> Khi Client nhận được JSON sản phẩm, nó sẽ có thêm các đường link (self, image, all-products)
    // ========================================================================
    @GetMapping("/public/products/search")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> searchProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) throws IOException {
        ProductResponse response = productService.searchProducts(keyword, categoryId, minPrice, maxPrice, pageNumber, pageSize, sortBy, sortOrder);
        
        List<EntityModel<ProductDTO>> productModels = response.getContent().stream()
                .map(dto -> {
                    try { return toModel(dto); } 
                    catch (IOException e) { throw new RuntimeException(e); }
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ProductDTO>> collectionModel = CollectionModel.of(productModels,
            linkTo(methodOn(ProductController.class).searchProducts(keyword, categoryId, minPrice, maxPrice, pageNumber, pageSize, sortBy, sortOrder)).withSelfRel());

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // ========================================================================
    // 2. THÊM SẢN PHẨM (ADMIN)
    // ========================================================================
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntityModel<ProductDTO>> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam("price") Double price,
            @RequestParam("discount") Double discount,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        
        Product product = new Product();
        product.setProductName(productName);
        product.setPrice(price);
        product.setDiscount(discount);
        product.setQuantity(quantity);
        product.setDescription(description);
        product.setImage(image != null && !image.isEmpty() ? saveImage(image) : "default.png");

        ProductDTO savedProduct = productService.addProduct(categoryId, product);
        EntityModel<ProductDTO> model = toModel(savedProduct);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    // ========================================================================
    // 3. CẬP NHẬT SẢN PHẨM (ADMIN)
    // ========================================================================
    @PostMapping(value = "/products/update/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntityModel<ProductDTO>> updateProduct(
            @PathVariable Long productId,
            @RequestParam("productName") String productName,
            @RequestParam("price") Double price,
            @RequestParam("discount") Double discount,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("description") String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        
        Product product = new Product();
        product.setProductName(productName);
        product.setPrice(price);
        product.setDiscount(discount);
        product.setQuantity(quantity);
        product.setDescription(description);
        
        if (categoryId != null) product.setCategoryId(categoryId); 
        if (image != null && !image.isEmpty()) product.setImage(saveImage(image));

        ProductDTO updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok(toModel(updatedProduct));
    }

    // ========================================================================
    // 4. HIỂN THỊ ẢNH & LƯU ẢNH
    // ========================================================================
    @GetMapping("/products/image/{fileName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) throws IOException {
        Path filePath = Paths.get("images").resolve(fileName);
        if (!Files.exists(filePath)) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

    private String saveImage(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("images"); 
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    // ========================================================================
    // 5. CÁC API CƠ BẢN (GET ONE, DELETE, GET ALL)
    // ========================================================================

    @GetMapping("/products/{productId}")
    public ResponseEntity<EntityModel<ProductDTO>> getProductById(@PathVariable Long productId) throws IOException {
        return ResponseEntity.ok(toModel(productService.getProductById(productId)));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) String pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) String pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder) throws IOException {

        ProductResponse res = productService.getAllProducts(Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sortBy, sortOrder);
        
        List<EntityModel<ProductDTO>> productModels = res.getContent().stream()
                .map(dto -> {
                    try { return toModel(dto); } 
                    catch (IOException e) { throw new RuntimeException(e); }
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ProductDTO>> collectionModel = CollectionModel.of(productModels,
            linkTo(methodOn(ProductController.class).getAllProducts(pageNumber, pageSize, sortBy, sortOrder)).withSelfRel());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(res.getTotalElements()))
                .body(collectionModel);
    }

    // ========================================================================
    // 6. TƯƠNG THÍCH MOBILE APP
    // ========================================================================

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder) throws IOException {
        
        ProductResponse res = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        
        List<EntityModel<ProductDTO>> models = res.getContent().stream()
                .map(dto -> {
                    try { return toModel(dto); } 
                    catch (IOException e) { throw new RuntimeException(e); }
                }).collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(models, 
            linkTo(methodOn(ProductController.class).getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder)).withSelfRel()));
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> searchProductByKeyword(
            @PathVariable String keyword,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder,
            @RequestParam(defaultValue = "0") Long categoryId) throws IOException { 
        
        ProductResponse res = productService.searchProductByKeyword(keyword, categoryId, pageNumber, pageSize, "id".equals(sortBy) ? "productId" : sortBy, sortOrder);
        
        List<EntityModel<ProductDTO>> models = res.getContent().stream()
                .map(dto -> {
                    try { return toModel(dto); } 
                    catch (IOException e) { throw new RuntimeException(e); }
                }).collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(ProductController.class).searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder, categoryId)).withSelfRel()));
    }
}