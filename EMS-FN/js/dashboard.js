$(document).ready(function() {
    // Initialize variables
    let selectedRow = null;
    let selectedEmployeeId = null;

    // Load employees on page load
    loadEmployees();

    // Image preview functionality
    $('#eimage').change(function() {
        const file = this.files[0];
        if (file) {
            if (file.size > 10 * 1024 * 1024) { // 10MB limit
                alert('Image size should be less than 10MB');
                $(this).val('');
                return;
            }
            
            const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!validTypes.includes(file.type)) {
                alert('Only JPEG, PNG, and GIF images are allowed');
                $(this).val('');
                return;
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                $('#image-preview').attr('src', e.target.result).show();
            }
            reader.readAsDataURL(file);
        } else {
            $('#image-preview').hide();
        }
    });

    // Row selection
    $(document).on("click", "#employee-table tbody tr", function() {
        if (selectedRow) {
            selectedRow.removeClass('table-active');
        }
        $(this).addClass('table-active');
        selectedRow = $(this);

        const cells = $(this).find("td");
        selectedEmployeeId = cells.eq(0).text();
        
        $("#eid").val(selectedEmployeeId);
        $("#ename").val(cells.eq(1).text());
        $("#enumber").val(cells.eq(2).text());
        $("#eaddress").val(cells.eq(3).text());
        $("#edepartment").val(cells.eq(4).text());
        $("#estatus").val(cells.eq(5).text());
        
        // Handle image preview for selected employee
        const imgElement = cells.eq(6).find("img");
        if (imgElement.length > 0 && imgElement.attr("src") !== "#") {
            $('#image-preview').attr('src', imgElement.attr('src')).show();
        } else {
            $('#image-preview').hide();
        }
    });

    // Save employee
    $('#save-employee').on('click', function() {
        const formData = new FormData();
        formData.append('ename', $('#ename').val());
        formData.append('enumber', $('#enumber').val());
        formData.append('eaddress', $('#eaddress').val());
        formData.append('edepartment', $('#edepartment').val());
        formData.append('estatus', $('#estatus').val());
        
        const imageFile = $('#eimage')[0].files[0];
        if (imageFile) {
            formData.append('eimage', imageFile);
        }

        // Validation
        if (!validateEmployeeForm(formData)) {
            return;
        }

        $.ajax({
            method: 'POST',
            url: 'http://localhost:8084/EMS_Web_exploded/employee',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                if (response.code === '200') {
                    showAlert('success', 'Employee saved successfully!');
                    resetForm();
                    loadEmployees();
                } else {
                    showAlert('danger', 'Error: ' + response.message);
                }
            },
            error: function(xhr) {
                showAlert('danger', 'Failed to save employee: ' + 
                    (xhr.responseJSON?.message || 'Unknown error'));
            }
        });
    });

    // Update employee
    $('#update-employee').click(function() {
        if (!selectedEmployeeId) {
            showAlert('warning', 'Please select an employee to update');
            return;
        }

        const formData = new FormData();
        formData.append('eid', selectedEmployeeId);
        formData.append('ename', $('#ename').val());
        formData.append('enumber', $('#enumber').val());
        formData.append('eaddress', $('#eaddress').val());
        formData.append('edepartment', $('#edepartment').val());
        formData.append('estatus', $('#estatus').val());
        
        const imageFile = $('#eimage')[0].files[0];
        if (imageFile) {
            formData.append('eimage', imageFile);
        }

        // Validation
        if (!validateEmployeeForm(formData)) {
            return;
        }

        $.ajax({
            url: "http://localhost:8084/EMS_Web_exploded/employee",
            method: "PUT",
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                if (response.code === "200") {
                    showAlert('success', 'Employee updated successfully!');
                    resetForm();
                    loadEmployees();
                } else {
                    showAlert('danger', 'Error: ' + response.message);
                }
            },
            error: function(xhr) {
                showAlert('danger', 'Failed to update employee: ' + 
                    (xhr.responseJSON?.message || 'Unknown error'));
            }
        });
    });

    // Delete employee
    $('#delete-employee').click(function() {
        if (!selectedEmployeeId) {
            showAlert('warning', 'Please select an employee to delete');
            return;
        }

        if (!confirm("Are you sure you want to delete this employee?")) {
            return;
        }

        $.ajax({
            url: "http://localhost:8084/EMS_Web_exploded/employee?eid=" + encodeURIComponent(selectedEmployeeId),
            method: "DELETE",
            success: function(response) {
                if (response.code === "200") {
                    showAlert('success', 'Employee deleted successfully!');
                    resetForm();
                    loadEmployees();
                } else {
                    showAlert('danger', 'Error: ' + response.message);
                }
            },
            error: function(xhr) {
                showAlert('danger', 'Failed to delete employee: ' + 
                    (xhr.responseJSON?.message || 'Unknown error'));
            }
        });
    });

    // Refresh Form
    $('#refresh-employee').click(function() {
        resetForm();
    });

    // Helper functions
    function loadEmployees() {
        $.ajax({
            url: "http://localhost:8084/EMS_Web_exploded/employee",
            method: "GET",
            success: function(response) {
                const tbody = $('#employee-table tbody');
                tbody.empty();

                if (response.data && response.data.length > 0) {
                    response.data.forEach(function(employee) {
                        const imageCell = employee.eimage ? 
                            `<td><img src="${employee.eimage}" alt="Employee Image" class="img-thumbnail" style="max-width: 80px;"></td>` : 
                            '<td>No Image</td>';
                        
                        tbody.append(
                            `<tr>
                                <td>${employee.eid}</td>
                                <td>${employee.ename}</td>
                                <td>${employee.enumber}</td>
                                <td>${employee.eaddress}</td>
                                <td>${employee.edepartment}</td>
                                <td>${employee.estatus}</td>
                                ${imageCell}
                            </tr>`
                        );
                    });
                } else {
                    tbody.append(
                        `<tr>
                            <td colspan="7" class="text-center">No employees found</td>
                        </tr>`
                    );
                }
            },
            error: function(error) {
                showAlert('danger', 'Failed to load employee data');
                console.error("Error:", error);
            }
        });
    }

    function validateEmployeeForm(formData) {
        const ename = formData.get('ename');
        const enumber = formData.get('enumber');
        const eaddress = formData.get('eaddress');
        const edepartment = formData.get('edepartment');
        const estatus = formData.get('estatus');

        if (!ename || !enumber || !eaddress || !edepartment || !estatus) {
            showAlert('warning', 'Please fill in all required fields');
            return false;
        }

        if (!/^\d{10}$/.test(enumber)) {
            showAlert('warning', 'Please enter a valid 10-digit mobile number');
            return false;
        }

        return true;
    }

    function resetForm() {
        $("#employee-form")[0].reset();
        $('#image-preview').hide();
        
        if (selectedRow) {
            selectedRow.removeClass('table-active');
            selectedRow = null;
        }
        
        selectedEmployeeId = null;
    }

    function showAlert(type, message) {
        const alertHtml = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        
        // Remove any existing alerts
        $('.alert').remove();
        
        // Add new alert before the form
        $('#employee-form').before(alertHtml);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            $('.alert').alert('close');
        }, 5000);
    }
});


//     function fetchEmployees() {
//         $.ajax({
//             method: 'GET',
//             url: 'http://localhost:8084/EMS_Web_exploded/employee',
//             success: function(response) {
//                 if (response.code === '200') {
//                     var employees = response.data;
//                     var employeeTable = $('#employee-table tbody');
//                     employeeTable.empty(); 
//                     employees.forEach(function(employee) {
//                         employeeTable.append(
//                             `<tr>
//                                 <td>
//                                     <button class="btn btn-primary" onclick="editEmployee(${employee.eid})">Edit</button>
//                                     <button class="btn btn-danger" onclick="deleteEmployee(${employee.eid})">Delete</button>
//                                 </td>
//                                 <td>${employee.ename}</td>
//                                 <td>${employee.enumber}</td>
//                                 <td>${employee.eaddress}</td>
//                                 <td>${employee.edepartment}</td>
//                                 <td>${employee.estatus}</td>
                                
//                             </tr>`
//                         );
//                     });
//                 } else {
//                     alert('Error fetching employees: ' + response.message);
//                 }
//             },
//             error: function() {
//                 alert('Failed to fetch employees.');
//             }
//         });
//     }
// });