$(document).ready(function() {
    // Load employees on page load
    $.ajax({
        url: 'http://localhost:8084/EMS_Web_exploded/employee',
        method: 'GET',
        success: function (response) {
            var tbody = $('#employee-table-tbody');
            tbody.empty();
            
            response.data.forEach(function(employee) {
                tbody.append(
                    '<tr>' +
                    '<td>' + employee.eid + '</td>' +
                    '<td>' + employee.ename + '</td>' +
                    '<td>' + employee.enumber + '</td>' +
                    '<td>' + employee.eaddress + '</td>' +
                    '<td>' + employee.edepartment + '</td>' +
                    '<td>' + employee.estatus + '</td>' +
                    '</tr>'
                );
            });
        },
        error: function (error) {
            console.log("Failed to load employee data:", error);
        }
    });

    // Row selection
    let selectedRow = null;
    $(document).on("click", "#employee-table-tbody tr", function() {
        if (selectedRow) {
            selectedRow.css('background-color', '');
        }
        $(this).css('background-color', '#97da99');
        selectedRow = $(this);

        const cells = $(this).find("td");
        $("#eid").val(cells.eq(0).text());
        $("#ename").val(cells.eq(1).text());
        $("#enumber").val(cells.eq(2).text());
        $("#eaddress").val(cells.eq(3).text());
        $("#edepartment").val(cells.eq(4).text());
        $("#estatus").val(cells.eq(5).text());
    });

    // Save employee
    $('#save-employee').on('click', function () {
        const employee = {
            ename: $('#ename').val(),
            enumber: $('#enumber').val(),
            eaddress: $('#eaddress').val(),
            edepartment: $('#edepartment').val(),
            estatus: $('#estatus').val()
        };

        if (!employee.ename || !employee.enumber || !employee.eaddress || !employee.edepartment || !employee.estatus) {
            alert("Please fill in all fields.");
            return;
        }

        if (!/^\d{10}$/.test(employee.enumber)) {
            alert("Please enter a valid 10-digit mobile number.");
            return;
        }

        $.ajax({
            method: 'POST',
            url: 'http://localhost:8084/EMS_Web_exploded/employee',
            contentType: 'application/json',
            data: JSON.stringify(employee),
            success: function (response) {
                if (response.code === '200') {
                    alert('Employee saved successfully!');
                    $("#employee-form")[0].reset();
                    reloadEmployeeTable();
                } else {
                    alert('Error: ' + response.message);
                }
            },
            error: function (error) {
                alert('Failed to save employee: ' + (error.responseJSON?.message || 'Unknown error'));
            }
        });
    });

    // Update employee
    $('#update-employee').click(function (e) {
        e.preventDefault();

        const employee = {
            eid: $('#eid').val(),
            ename: $('#ename').val(),
            enumber: $('#enumber').val(),
            eaddress: $('#eaddress').val(),
            edepartment: $('#edepartment').val(),
            estatus: $('#estatus').val()
        };

        if (!employee.eid) {
            alert("Please select an employee to update.");
            return;
        }

        if (!employee.ename || !employee.enumber || !employee.eaddress || !employee.edepartment || !employee.estatus) {
            alert("Please fill in all fields.");
            return;
        }

        if (!/^\d{10}$/.test(employee.enumber)) {
            alert("Please enter a valid 10-digit mobile number.");
            return;
        }

        $.ajax({
            url: "http://localhost:8084/EMS_Web_exploded/employee",
            method: "PUT",
            contentType: "application/json",
            data: JSON.stringify(employee),
            success: function (response) {
                if (response.code === "200") {
                    alert("Employee Updated Successfully!");
                    $("#employee-form")[0].reset();
                    reloadEmployeeTable();
                } else {
                    alert("Error: " + response.message);
                }
            },
            error: function (xhr) {
                alert("Failed to Update Employee: " + (xhr.responseJSON?.message || "Unknown error"));
            }
        });
    });

    // Delete employee
    $('#delete-employee').click(function (e) {
        e.preventDefault();

        const eid = $('#eid').val();
        if (!eid) {
            alert("Please select an employee to delete.");
            return;
        }

        if (!confirm("Are you sure you want to delete this employee?")) {
            return;
        }

        $.ajax({
            url: "http://localhost:8084/EMS_Web_exploded/employee?eid=" + encodeURIComponent(eid),
            method: "DELETE",
            success: function (response) {
                if (response.code === "200") {
                    alert("Employee Deleted Successfully!");
                    $("#employee-form")[0].reset();
                    reloadEmployeeTable();
                } else {
                    alert("Error: " + response.message);
                }
            },
            error: function (xhr) {
                alert("Failed to Delete Employee: " + (xhr.responseJSON?.message || "Unknown error"));
            }
        });
    });

    // Refresh Form
    $('#refresh-employee').click(function (e) {
        e.preventDefault();
        $("#employee-form")[0].reset();
        if (selectedRow) {
            selectedRow.css('background-color', '');
            selectedRow = null;
        }
    });

    // Reload table
    function reloadEmployeeTable() {
        $.ajax({
            url: "http://localhost:8084/EMS_Web_exploded/employee",
            method: "GET",
            success: function (response) {
                const tbody = $('#employee-table-tbody');
                tbody.empty();

                response.data.forEach(function(employee) {
                    tbody.append(
                        '<tr>' +
                        '<td>' + employee.eid + '</td>' +
                        '<td>' + employee.ename + '</td>' +
                        '<td>' + employee.enumber + '</td>' +
                        '<td>' + employee.eaddress + '</td>' +
                        '<td>' + employee.edepartment + '</td>' +
                        '<td>' + employee.estatus + '</td>' +
                        '</tr>'
                    );
                });
            },
            error: function (error) {
                console.log("Failed to load employee data:", error);
            }
        });
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