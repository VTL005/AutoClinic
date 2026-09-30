CREATE TABLE invoices (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          invoice_code VARCHAR(30) NOT NULL,

                          repair_order_id BIGINT NOT NULL,

                          customer_user_id BIGINT NOT NULL,

                          vehicle_id BIGINT NOT NULL,

                          created_by_user_id BIGINT NOT NULL,

                          subtotal DECIMAL(15, 2) NOT NULL DEFAULT 0,

                          discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,

                          tax_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,

                          total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,

                          paid_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,

                          status VARCHAR(40) NOT NULL,

                          issued_at DATETIME NULL,

                          due_at DATETIME NULL,

                          paid_at DATETIME NULL,

                          note VARCHAR(1000) NULL,

                          version BIGINT NOT NULL DEFAULT 0,

                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          updated_at DATETIME NOT NULL
                              DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (id),

                          CONSTRAINT uk_invoices_invoice_code
                              UNIQUE (invoice_code),

                          CONSTRAINT uk_invoices_repair_order_id
                              UNIQUE (repair_order_id),

                          CONSTRAINT chk_invoices_subtotal
                              CHECK (subtotal >= 0),

                          CONSTRAINT chk_invoices_discount_amount
                              CHECK (discount_amount >= 0),

                          CONSTRAINT chk_invoices_tax_amount
                              CHECK (tax_amount >= 0),

                          CONSTRAINT chk_invoices_total_amount
                              CHECK (total_amount >= 0),

                          CONSTRAINT chk_invoices_paid_amount
                              CHECK (paid_amount >= 0),

                          INDEX idx_invoices_customer_user_id (
        customer_user_id
    ),

                          INDEX idx_invoices_vehicle_id (
        vehicle_id
    ),

                          INDEX idx_invoices_status (
        status
    ),

                          INDEX idx_invoices_created_at (
        created_at
    )
);

CREATE TABLE invoice_items (
                               id BIGINT NOT NULL AUTO_INCREMENT,

                               invoice_id BIGINT NOT NULL,

                               item_type VARCHAR(30) NOT NULL,

                               reference_id BIGINT NULL,

                               item_name VARCHAR(200) NOT NULL,

                               description VARCHAR(1000) NULL,

                               quantity DECIMAL(12, 2) NOT NULL,

                               unit_price DECIMAL(15, 2) NOT NULL,

                               line_total DECIMAL(15, 2) NOT NULL,

                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (id),

                               CONSTRAINT fk_invoice_items_invoice
                                   FOREIGN KEY (invoice_id)
                                       REFERENCES invoices (id)
                                       ON DELETE CASCADE,

                               CONSTRAINT chk_invoice_items_quantity
                                   CHECK (quantity > 0),

                               CONSTRAINT chk_invoice_items_unit_price
                                   CHECK (unit_price >= 0),

                               CONSTRAINT chk_invoice_items_line_total
                                   CHECK (line_total >= 0),

                               INDEX idx_invoice_items_invoice_id (
        invoice_id
    ),

                               INDEX idx_invoice_items_reference (
        item_type,
        reference_id
    )
);

CREATE TABLE payments (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          payment_code VARCHAR(30) NOT NULL,

                          invoice_id BIGINT NOT NULL,

                          amount DECIMAL(15, 2) NOT NULL,

                          payment_method VARCHAR(40) NOT NULL,

                          status VARCHAR(40) NOT NULL,

                          transaction_reference VARCHAR(150) NULL,

                          paid_at DATETIME NULL,

                          received_by_user_id BIGINT NOT NULL,

                          note VARCHAR(1000) NULL,

                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          PRIMARY KEY (id),

                          CONSTRAINT uk_payments_payment_code
                              UNIQUE (payment_code),

                          CONSTRAINT fk_payments_invoice
                              FOREIGN KEY (invoice_id)
                                  REFERENCES invoices (id),

                          CONSTRAINT chk_payments_amount
                              CHECK (amount > 0),

                          INDEX idx_payments_invoice_id (
        invoice_id
    ),

                          INDEX idx_payments_status (
        status
    ),

                          INDEX idx_payments_created_at (
        created_at
    )
);