package teamnova.elite_gear.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import teamnova.elite_gear.domain.Category;
import teamnova.elite_gear.domain.Product;
import teamnova.elite_gear.model.CategoryDTO;
import teamnova.elite_gear.repos.CategoryRepository;
import teamnova.elite_gear.repos.ProductRepository;
import teamnova.elite_gear.util.NotFoundException;
import teamnova.elite_gear.util.ReferencedWarning;


@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(final CategoryRepository categoryRepository,
            final ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryDTO> findAll() {
        final List<Category> categories = categoryRepository.findAll(Sort.by("categoryName"));
        return categories.stream()
                .map(category -> mapToDTO(category, new CategoryDTO()))
                .toList();
    }

    public CategoryDTO get(final UUID orderItemID) {
        return categoryRepository.findById(orderItemID)
                .map(category -> mapToDTO(category, new CategoryDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public List<CategoryDTO> getParentCategories() {
        final List<Category> categories = categoryRepository.findByParentCategoryIsNull();
        return categories.stream()
                .map(category -> mapToDTO(category, new CategoryDTO()))
                .toList();
    }

    public List<CategoryDTO> getSubcategories(final UUID parentCategoryID) {
        final List<Category> categories = categoryRepository.findByParentCategory(categoryRepository.findById(parentCategoryID).orElseThrow(NotFoundException::new));
        return categories.stream()
                .map(category -> mapToDTO(category, new CategoryDTO()))
                .toList();
    }

    public UUID create(final CategoryDTO categoryDTO) {
        final Category category = new Category();
        mapToEntity(categoryDTO, category);
        return categoryRepository.save(category).getCategoryId();
    }

    public void update(final UUID orderItemID, final CategoryDTO categoryDTO) {
        final Category category = categoryRepository.findById(orderItemID)
                .orElseThrow(NotFoundException::new);
        mapToEntity(categoryDTO, category);
        categoryRepository.save(category);
    }

    public void delete(final UUID orderItemID) {
        categoryRepository.deleteById(orderItemID);
    }

    private CategoryDTO mapToDTO(final Category category, final CategoryDTO categoryDTO) {
        categoryDTO.setCategoryID(category.getCategoryId());
        categoryDTO.setCategoryName(category.getCategoryName());
        categoryDTO.setImageUrl(category.getImageUrl());
        categoryDTO.setParentCategoryID(category.getParentCategory() != null ? category.getParentCategory().getCategoryId() : null);
        return categoryDTO;
    }

    private Category mapToEntity(final CategoryDTO categoryDTO, final Category category) {
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setImageUrl(categoryDTO.getImageUrl());
        category.setParentCategory(categoryDTO.getParentCategoryID() != null ? categoryRepository.findById(categoryDTO.getParentCategoryID()).orElseThrow(NotFoundException::new) : null);
        return category;
    }

    public ReferencedWarning getReferencedWarning(final UUID orderItemID) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Category category = categoryRepository.findById(orderItemID)
                .orElseThrow(NotFoundException::new);
        final Product categoryProduct = productRepository.findFirstByCategory(category);
        if (categoryProduct != null) {
            referencedWarning.setKey("category.product.category.referenced");
            referencedWarning.addParam(categoryProduct.getProductID());
            return referencedWarning;
        }
        return null;
    }

}
